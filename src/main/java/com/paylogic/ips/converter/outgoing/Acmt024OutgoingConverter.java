package com.paylogic.ips.converter.outgoing;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gms.utils.common.RandomUtil;
import com.gms.utils.common.StringUtil;
import com.paylogic.ama.core.model.AccountInfo;
import com.paylogic.ama.core.model.CustomerKyc;
import com.paylogic.ama.core.model.ParameterCategory;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ips.converter.DocumentConverter;
import com.paylogic.ips.iso20022.bo.acmt024.AccountIdentification4Choice;
import com.paylogic.ips.iso20022.bo.acmt024.AccountSchemeName1Choice;
import com.paylogic.ips.iso20022.bo.acmt024.BranchAndFinancialInstitutionIdentification6;
import com.paylogic.ips.iso20022.bo.acmt024.CashAccount40;
import com.paylogic.ips.iso20022.bo.acmt024.DocumentAcmt024;
import com.paylogic.ips.iso20022.bo.acmt024.FinancialInstitutionIdentification18;
import com.paylogic.ips.iso20022.bo.acmt024.GenericAccountIdentification1;
import com.paylogic.ips.iso20022.bo.acmt024.GenericFinancialIdentification1;
import com.paylogic.ips.iso20022.bo.acmt024.IdentificationAssignment3;
import com.paylogic.ips.iso20022.bo.acmt024.IdentificationInformation4;
import com.paylogic.ips.iso20022.bo.acmt024.IdentificationVerificationReportV03;
import com.paylogic.ips.iso20022.bo.acmt024.MessageIdentification7;
import com.paylogic.ips.iso20022.bo.acmt024.Party40Choice;
import com.paylogic.ips.iso20022.bo.acmt024.PartyIdentification135;
import com.paylogic.ips.iso20022.bo.acmt024.PostalAddress24;
import com.paylogic.ips.iso20022.bo.acmt024.ProxyAccountIdentification1;
import com.paylogic.ips.iso20022.bo.acmt024.VerificationReason1Choice;
import com.paylogic.ips.iso20022.bo.acmt024.VerificationReport4;
import com.paylogic.ips.iso20022.bo.pacs.AcmtBaseDocument;
import com.paylogic.ips.model.PaymentExtended;
import com.paylogic.ips.store.IsoParameterStore;
import com.paylogic.ips.util.CoreUtil;

@Component
public class Acmt024OutgoingConverter extends DocumentConverter{
	private static final Logger LOG = Logger.getLogger(Acmt024OutgoingConverter.class.getName());
	@Autowired
	private IsoParameterStore paramStore;
	
	public DocumentAcmt024 convertFromPaymentResponse(PaymentExtended payment,List<ParameterCategory> cats, AcmtBaseDocument initialTrx) {
		IdentificationVerificationReportV03 acmt024 = new IdentificationVerificationReportV03();
		acmt024.setAssgnmt(createAssignement(payment,initialTrx));
		acmt024.setOrgnlAssgnmt(createOriginalAssgnmt(payment,initialTrx));
		
		acmt024.getRpt().add(createVerificationReport(payment,paramStore.getStatusMapping(),initialTrx));
		//create document
		DocumentAcmt024 document = new DocumentAcmt024();
		document.setIdVrfctnRpt(acmt024);
		//include message
		includeMessage(document, cats, payment, false);
		return document;
	}
	
	private IdentificationAssignment3 createAssignement(Payment payment, AcmtBaseDocument document) {
		IdentificationAssignment3 header = new IdentificationAssignment3();
		String msgId = payment.getAcquirerTrxRef();
		if(StringUtil.isNullOrEmpty(msgId)) {
			//since it is mandatory, generate new one
			msgId = RandomUtil.randomUuid();
		}
		header.setMsgId(msgId);
		Date createTime = payment.getUpdateTime() == null ? new Date() : payment.getUpdateTime();
		try {
			header.setCreDtTm(CoreUtil.convertDateToXmlGregorienDate(createTime,true));
		} catch (DatatypeConfigurationException e) {
			LOG.info("createAssignement::cannot convert date "+createTime+":"+e);
		}
		if(!StringUtil.isNullOrEmpty(document.getAssgneId())) {
			header.setAssgne(copyInst(document.getAssgneId()));
		}
		if(!StringUtil.isNullOrEmpty(document.getAssgnrId())) {
			header.setAssgnr(copyInst(document.getAssgnrId()));
		}
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

	private MessageIdentification7 createOriginalAssgnmt(Payment payment, AcmtBaseDocument document) {
		MessageIdentification7 orig = new MessageIdentification7();
		orig.setMsgId(document.getMsgId());
		orig.setCreDtTm(document.getCreDtTm());
		return orig;
	}
	
	private VerificationReport4 createVerificationReport(PaymentExtended payment,Map<String, String> statusMapping, AcmtBaseDocument initialTrx) {
		VerificationReport4 report = new VerificationReport4();
		report.setOrgnlId(initialTrx.getVerificationId());
		report.setVrfctn(CoreUtil.PAY_STATUS_ACCEPTED.equals(payment.getState()));
		if(CoreUtil.PAY_STATUS_REJECTED.equals(payment.getState()) ||
				CoreUtil.PAY_STATUS_SUSPECTED.equals(payment.getState())) {
			report.setRsn(createVerificationReason(payment));
		}else {
			//add wallet/account info
			IdentificationInformation4 ident = createUpdatedAccountInfo(payment); 
			report.setOrgnlPtyAndAcctId(ident);
			report.setUpdtdPtyAndAcctId(ident);
		}
		return report;
	}

	private IdentificationInformation4 createUpdatedAccountInfo(PaymentExtended payment) {
		//AE to solve the issue of wallet destination not set despite being present in outgoing payment for outgoing acmt024
		IdentificationInformation4 identification = new IdentificationInformation4();
		if(payment.getWalletDestination() != null) {
			identification.setAcct(createWalletInfo(payment));
		}
		else if (payment.getDstAccounts() != null && !payment.getDstAccounts().isEmpty()) {
			identification.setAcct(createAccountInfo(payment));
			}
		else {
			return null;
		}
		identification.setPty(createPartyInformation(payment));
		return identification;
	}

	private CashAccount40 createWalletInfo(PaymentExtended payment) {
		CashAccount40 acc  = new CashAccount40();
		AccountIdentification4Choice accId = new AccountIdentification4Choice();
		GenericAccountIdentification1 gen = new GenericAccountIdentification1();
		gen.setId(payment.getWalletDestination());
		AccountSchemeName1Choice schm = new AccountSchemeName1Choice();
		schm.setPrtry(CoreUtil.ISO22_EWALLET_TYPE);
		gen.setSchmeNm(schm);
		accId.setOthr(gen);
		acc.setId(accId);
		return acc;
	}

	private PartyIdentification135 createPartyInformation(PaymentExtended payment) {
		CustomerKyc kyc = payment.getReceiverCustomerData();
		if(kyc != null) {
			PartyIdentification135 pty = new PartyIdentification135();
			pty.setNm(kyc.getFirstname());
			if(!StringUtil.isNullOrEmpty(kyc.getAddress())) {
				PostalAddress24 pstl = new PostalAddress24();
				pstl.setCtry(kyc.getCountry());
				pstl.setTwnNm(kyc.getCity());
				if(!StringUtil.isNullOrEmpty(kyc.getAddress())) {
					for(String line : kyc.getAddress().split("\n")) {
						pstl.getAdrLine().add(line);
					}
				}
				pty.setPstlAdr(pstl);
			}
			return pty;
		}else {
			return null;
		}
		
	}

	private CashAccount40 createAccountInfo(PaymentExtended payment) {
		AccountInfo info = payment.getDstAccounts().get(0);
		CashAccount40 acc  = new CashAccount40();
		acc.setCcy(info.getCurrency());
		acc.setNm(info.getName());
		ProxyAccountIdentification1 prx = new ProxyAccountIdentification1();
		prx.setId(info.getAlias());
		acc.setPrxy(prx);
		AccountIdentification4Choice accId = new AccountIdentification4Choice();
		GenericAccountIdentification1 gen = new GenericAccountIdentification1();
		gen.setId(info.getIden());
		AccountSchemeName1Choice schm = new AccountSchemeName1Choice();
		schm.setPrtry(CoreUtil.ACC_INFO_ACCOUNT.equals(info.getType()) ? CoreUtil.ISO22_ACCOUNT_TYPE : CoreUtil.ISO22_EWALLET_TYPE);
		gen.setSchmeNm(schm);
		accId.setOthr(gen);
		acc.setId(accId);
		return acc;
	}

	private VerificationReason1Choice createVerificationReason(PaymentExtended payment) {
		VerificationReason1Choice status = new VerificationReason1Choice();
		String code = paramStore.getRejectMapping().get(payment.getRejectCode());
		LOG.trace("createVerificationReason::converted reject code "+code);
		if(StringUtil.isNullOrEmpty(code)) {
			code = paramStore.getDefaultRejectCode();
		}
		LOG.trace("createVerificationReason::final converted reject code "+code);
		status.setCd(code);
		String addInf = null;
		if("X050".equals(code)) {
			addInf = "No data found";
		}else {
			addInf = payment.getRejectMessage();
		}
		if(!StringUtil.isNullOrEmpty(addInf)) {
			addInf = addInf.replace("<", "'").replace(">", "'"); 
		}
		status.setPrtry(addInf);
		return status;
	}

}
