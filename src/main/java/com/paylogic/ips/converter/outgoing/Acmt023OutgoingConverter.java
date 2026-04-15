package com.paylogic.ips.converter.outgoing;

import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gms.utils.common.RandomUtil;
import com.gms.utils.common.StringUtil;
import com.paylogic.ama.core.model.ParameterCategory;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ips.converter.DocumentConverter;
import com.paylogic.ips.iso20022.bo.acmt23.AccountIdentification4Choice;
import com.paylogic.ips.iso20022.bo.acmt23.AccountSchemeName1Choice;
import com.paylogic.ips.iso20022.bo.acmt23.BranchAndFinancialInstitutionIdentification6;
import com.paylogic.ips.iso20022.bo.acmt23.CashAccount40;
import com.paylogic.ips.iso20022.bo.acmt23.DocumentAcmt023;
import com.paylogic.ips.iso20022.bo.acmt23.FinancialInstitutionIdentification18;
import com.paylogic.ips.iso20022.bo.acmt23.GenericAccountIdentification1;
import com.paylogic.ips.iso20022.bo.acmt23.GenericFinancialIdentification1;
import com.paylogic.ips.iso20022.bo.acmt23.IdentificationAssignment3;
import com.paylogic.ips.iso20022.bo.acmt23.IdentificationInformation4;
import com.paylogic.ips.iso20022.bo.acmt23.IdentificationVerification4;
import com.paylogic.ips.iso20022.bo.acmt23.IdentificationVerificationRequestV03;
import com.paylogic.ips.iso20022.bo.acmt23.Party40Choice;
import com.paylogic.ips.store.IsoParameterStore;
import com.paylogic.ips.util.CoreUtil;

@Component
public class Acmt023OutgoingConverter extends DocumentConverter{
	private static final Logger LOG = Logger.getLogger(Acmt023OutgoingConverter.class.getName());
	@Autowired
	IsoParameterStore handlerStore;
	
	public DocumentAcmt023 convertPayment(Payment payment,List<ParameterCategory> cats) {
		IdentificationVerificationRequestV03 acmt023 = new IdentificationVerificationRequestV03();
		//set header
		acmt023.setAssgnmt(createAssignement(payment));
		//set body
		acmt023.getVrfctn().add(createVerification(payment,cats));
		//create document
		DocumentAcmt023 document = new DocumentAcmt023();
		document.setIdVrfctnReq(acmt023);
		//include message
		includeMessage(document, cats, payment, false);
		return document;
	}
	
	private IdentificationVerification4 createVerification(Payment payment, List<ParameterCategory> cats) {
		IdentificationVerification4 verif = new IdentificationVerification4();
		verif.setId(payment.getVoucherCode());
		verif.setPtyAndAcctId(createIdentificationInformation(payment,cats));
		return verif;
	}

	private IdentificationInformation4 createIdentificationInformation(Payment payment, List<ParameterCategory> cats) {
		IdentificationInformation4 ident = new IdentificationInformation4();
		CashAccount40 acc = new CashAccount40();
		AccountIdentification4Choice accIdent = new AccountIdentification4Choice();
		GenericAccountIdentification1 othr = new GenericAccountIdentification1();
		AccountSchemeName1Choice schm = new AccountSchemeName1Choice();
		if(payment.getDstAccounts() == null || payment.getDstAccounts().isEmpty()) {
			othr.setId(payment.getWalletDestination());
			schm.setPrtry(CoreUtil.ISO22_EWALLET_TYPE);
		}else {
			othr.setId(payment.getDstAccounts().get(0).getIden());
			schm.setPrtry(CoreUtil.ACC_INFO_ACCOUNT.equals(payment.getDstAccounts().get(0).getType()) ? CoreUtil.ISO22_ACCOUNT_TYPE : CoreUtil.ISO22_EWALLET_TYPE);
		}
		othr.setSchmeNm(schm);
		accIdent.setOthr(othr);
		acc.setId(accIdent);
		ident.setAcct(acc);
		return ident;
	}

	private IdentificationAssignment3 createAssignement(Payment payment) {
		IdentificationAssignment3 header = new IdentificationAssignment3();
		String msgId = payment.getIssuerTrxRef();
		if(StringUtil.isNullOrEmpty(msgId)) {
			//since it is mandatory, generate new one
			msgId = RandomUtil.randomUuid();
		}
		header.setMsgId(msgId);
		try {
			header.setCreDtTm(CoreUtil.convertDateToXmlGregorienDate(payment.getCreateTime(),true));
		} catch (DatatypeConfigurationException e) {
			LOG.info("createAssignement::cannot convert date "+payment.getCreateTime()+":"+e);
		}
		header.setAssgnr(copyInst(payment.getFromMember()));
		header.setAssgne(copyInst(payment.getToMember()));
		return header;
	}
	
	private Party40Choice copyInst( String id) {
		Party40Choice pty = new Party40Choice();
		BranchAndFinancialInstitutionIdentification6 inst = new BranchAndFinancialInstitutionIdentification6();
		FinancialInstitutionIdentification18 fin = new FinancialInstitutionIdentification18();
		GenericFinancialIdentification1 gen =new GenericFinancialIdentification1();
		gen.setId(id);
		fin.setOthr(gen);
		inst.setFinInstnId(fin);
		pty.setAgt(inst);
		return pty;
	}	
}
