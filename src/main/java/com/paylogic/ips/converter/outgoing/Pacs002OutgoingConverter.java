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
import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.WebRequest;
import com.paylogic.ama.core.exception.DoNotHonorEx;
import com.paylogic.ama.core.model.ParameterCategory;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ama.core.model.PaymentAdditionalData;
import com.paylogic.ama.core.model.PaymentAdditionalDataCat;
import com.paylogic.ips.converter.DocumentConverter;
import com.paylogic.ips.iso20022.bo.pacs.PacsBaseDocument;
import com.paylogic.ips.iso20022.bo.pacs002.AccountIdentification4Choice;
import com.paylogic.ips.iso20022.bo.pacs002.AccountSchemeName1Choice;
import com.paylogic.ips.iso20022.bo.pacs002.ActiveOrHistoricCurrencyAndAmount;
import com.paylogic.ips.iso20022.bo.pacs002.AmountType4Choice;
import com.paylogic.ips.iso20022.bo.pacs002.BranchAndFinancialInstitutionIdentification6;
import com.paylogic.ips.iso20022.bo.pacs002.CashAccount40;
import com.paylogic.ips.iso20022.bo.pacs002.DocumentPacs002;
import com.paylogic.ips.iso20022.bo.pacs002.FIToFIPaymentStatusReportV13;
import com.paylogic.ips.iso20022.bo.pacs002.FinancialInstitutionIdentification18;
import com.paylogic.ips.iso20022.bo.pacs002.GenericAccountIdentification1;
import com.paylogic.ips.iso20022.bo.pacs002.GenericFinancialIdentification1;
import com.paylogic.ips.iso20022.bo.pacs002.GroupHeader101;
import com.paylogic.ips.iso20022.bo.pacs002.OriginalGroupHeader17;
import com.paylogic.ips.iso20022.bo.pacs002.OriginalTransactionReference35;
import com.paylogic.ips.iso20022.bo.pacs002.PaymentTransaction142;
import com.paylogic.ips.iso20022.bo.pacs002.StatusReason6Choice;
import com.paylogic.ips.iso20022.bo.pacs002.StatusReasonInformation12;
import com.paylogic.ips.iso20022.bo.pacs003.DocumentPacs003;
import com.paylogic.ips.iso20022.bo.pacs008.DocumentPacs008;
import com.paylogic.ips.model.PaymentExtended;
import com.paylogic.ips.store.IsoParameterStore;
import com.paylogic.ips.util.CoreUtil;

@Component
public class Pacs002OutgoingConverter extends DocumentConverter{
	private static final Logger LOG = Logger.getLogger(Pacs002OutgoingConverter.class.getName());
	@Autowired
	private IsoParameterStore paramStore;
	
	public DocumentPacs002 convertFromPayment(Payment payment,List<ParameterCategory> cats) {
		PaymentExtended ext = new PaymentExtended(payment);
		Class<? extends BusinessException> cls = CoreUtil.exceptionMapping.get(payment.getProcessingReturnCode());
		if(cls == null) {
			LOG.info("convertFromPayment::no mapping for "+payment.getProcessingReturnCode());
			cls = DoNotHonorEx.class;
		}
		LOG.trace("convertFromPayment::converting reject code "+cls.getSimpleName());
		ext.setRejectCode(cls.getSimpleName());
		PacsBaseDocument initialTrx = extractOriginalMessage(ext);
		LOG.trace("convertFromPayment::original trx "+initialTrx);
		return initialTrx == null ? null : convertFromPayment(ext, cats, initialTrx);
	}
	
	public DocumentPacs002 convertFromPayment(PaymentExtended payment,List<ParameterCategory> cats, PacsBaseDocument initialTrx) {
		FIToFIPaymentStatusReportV13 pacs002 = new FIToFIPaymentStatusReportV13();
		pacs002.setGrpHdr(createHeader(payment,initialTrx.getInstgAgtId(),initialTrx.getInstdAgtId()));
		pacs002.getOrgnlGrpInfAndSts().add(createOriginalTrx(payment,initialTrx));
		
		pacs002.getTxInfAndSts().add(createPaymentInformation(payment,paramStore.getStatusMapping(),initialTrx));
		//create document
		DocumentPacs002 document = new DocumentPacs002();
		document.setFIToFIPmtStsRpt(pacs002);
		//include message
		includeMessage(document, cats, payment, false);
		return document;
	}
	
	private PacsBaseDocument extractOriginalMessage(PaymentExtended extPayment) {
		if(extPayment.getAdditionalData() == null || extPayment.getAdditionalData().isEmpty()) {
			LOG.info("extractOriginalMessage::empty additional data");
			return null;
		}
		DocumentPacs008 pacs008 = new DocumentPacs008();
		DocumentPacs003 pacs003 = new DocumentPacs003();
		PacsBaseDocument orig = null;
		for(PaymentAdditionalDataCat cat : extPayment.getAdditionalData()) {
			LOG.trace("extractOriginalMessage::analysing "+cat.getIden());
			if(cat.getIden().startsWith("IN@"+pacs008.getDocumentVersion())){
				orig = pacs008;
			}else if(cat.getIden().startsWith("IN@"+pacs003.getDocumentVersion())){
				orig = pacs003;
			}
			LOG.trace("extractOriginalMessage::orig "+orig);
			if(orig != null && cat.getAdditionalData() != null) {
				while(cat.getAdditionalData().iterator().hasNext()) {
					PaymentAdditionalData data = cat.getAdditionalData().iterator().next();
					LOG.trace("extractOriginalMessage::analysing "+data);
					if(ISO20022_MSG_DATA.equals(data.getKey())) {
						LOG.trace("extractOriginalMessage::found");
						WebRequest req = new WebRequest();
						orig = req.convertFromXML(data.getValue(), orig.getClass());
						LOG.trace("extractOriginalMessage::final orig "+orig);
						break;
					}
				}
			}else {
				LOG.trace("extractOriginalMessage::no additional data");
			}
		}
		return orig;
	}
	
	private GroupHeader101 createHeader(Payment payment, String fromMember,String toMember) {
		GroupHeader101 header = new GroupHeader101();
		String msgId = payment.getAcquirerTrxRef();//AE Pacs 002 is coming from acquirer
		if(StringUtil.isNullOrEmpty(msgId)) {
			//since it is mandatory, generate new one
			msgId = RandomUtil.randomUuid(); 
		}
		header.setMsgId(msgId);
		Date createTime = payment.getUpdateTime() == null ? new Date() : payment.getUpdateTime();
		try {
			header.setCreDtTm(CoreUtil.convertDateToXmlGregorienDate(createTime,true));
		} catch (DatatypeConfigurationException e) {
			LOG.info("createHeader::cannot convert date "+createTime+":"+e);
		}
		if(!StringUtil.isNullOrEmpty(toMember)) {
			header.setInstdAgt(copyInst(toMember));
		}
		if(!StringUtil.isNullOrEmpty(fromMember)) {
			header.setInstgAgt(copyInst(fromMember));
		}
		return header;
	}
	
	private BranchAndFinancialInstitutionIdentification6 copyInst( String id) {
		BranchAndFinancialInstitutionIdentification6 inst = new BranchAndFinancialInstitutionIdentification6();
		FinancialInstitutionIdentification18 fin = new FinancialInstitutionIdentification18();
		GenericFinancialIdentification1 gen =new GenericFinancialIdentification1();
		gen.setId(id);
		fin.setOthr(gen);
		inst.setFinInstnId(fin);
		return inst;
	}

	private OriginalGroupHeader17 createOriginalTrx(Payment payment, PacsBaseDocument document) {
		OriginalGroupHeader17 orig = new OriginalGroupHeader17();
		orig.setOrgnlMsgId(document.getMsgId());
		orig.setOrgnlMsgNmId(document.getDocumentVersion());
		orig.setOrgnlCreDtTm(document.getCreDtTm());
		return orig;
	}
	
	private PaymentTransaction142 createPaymentInformation(PaymentExtended payment,Map<String, String> statusMapping, PacsBaseDocument initialTrx) {
		PaymentTransaction142 trx = new PaymentTransaction142();
		String val = statusMapping.get( payment.getState());
		if(StringUtil.isNullOrEmpty(val)) {
			LOG.info("createPaymentInformation::invalid state "+payment.getState());
			val = "BLCK";
		}
		trx.setTxSts(val);
		Date updateTime = payment.getUpdateTime() == null ? new Date() : payment.getUpdateTime();
		try {
			trx.setAccptncDtTm(CoreUtil.convertDateToXmlGregorienDate(updateTime,true));
		} catch (DatatypeConfigurationException e) {
			LOG.info("createPaymentInformation::cannot convert date "+updateTime+":"+e);
		}
		if(CoreUtil.PAY_STATUS_REJECTED.equals(payment.getState()) ||
				CoreUtil.PAY_STATUS_SUSPECTED.equals(payment.getState())) {
			trx.getStsRsnInf().add(createStatusReason(payment,val));
		}
		trx.setOrgnlEndToEndId("-");
		trx.setOrgnlTxId(initialTrx.getTransactionId());
		trx.setClrSysRef(StringUtil.isNullOrEmpty(initialTrx.getClearingSysRef())?payment.getVoucherCode() : initialTrx.getClearingSysRef());
		trx.setOrgnlTxRef(createOriginalTrxRef(payment,initialTrx));
		return trx;
	}

	private OriginalTransactionReference35 createOriginalTrxRef(Payment payment, PacsBaseDocument initialTrx) {
		OriginalTransactionReference35 ref = new OriginalTransactionReference35();
		ActiveOrHistoricCurrencyAndAmount activeAmnt;
		if(initialTrx.getTransactionAmount() != null) {
			AmountType4Choice amnt = new AmountType4Choice();
			activeAmnt = new ActiveOrHistoricCurrencyAndAmount();
			activeAmnt.setCcy(initialTrx.getTransactionCurrency());
			activeAmnt.setValue(initialTrx.getTransactionAmount());
			amnt.setInstdAmt(activeAmnt);
			ref.setAmt(amnt);
		}
		if(initialTrx.getInterBankAmount() != null) {
			activeAmnt = new ActiveOrHistoricCurrencyAndAmount();
			activeAmnt.setCcy(initialTrx.getInterBankCurrency());
			activeAmnt.setValue(initialTrx.getInterBankAmount());
			ref.setIntrBkSttlmAmt(activeAmnt);
		}
		//Set debtor
//		if(initialTrx.getCdtTrfTxInf().get(0).getDbtr() != null) {
//			PartyIdentification135 dbtr = new PartyIdentification135();
//			dbtr.setNm(initialTrx.getCdtTrfTxInf().get(0).getDbtr().getNm());
//			if(initialTrx.getCdtTrfTxInf().get(0).getDbtr().getPstlAdr() != null) {
//				PostalAddress24 pstl = new PostalAddress24();
//				pstl.getAdrLine().addAll(initialTrx.getCdtTrfTxInf().get(0).getDbtr().getPstlAdr().getAdrLine());
//				dbtr.setPstlAdr(pstl);
//			}
//			ref.setDbtr(dbtr);
//		}
		// debtor account
		if(initialTrx.getDebtorAccountId() != null) {
			CashAccount40 dbtrAcc  =new CashAccount40();
			dbtrAcc.setCcy(initialTrx.getDebtorAccountCurrency());
			AccountIdentification4Choice accId = new AccountIdentification4Choice();
			GenericAccountIdentification1 gen = new GenericAccountIdentification1();
			gen.setId(initialTrx.getDebtorAccountId());
			AccountSchemeName1Choice schm = new AccountSchemeName1Choice();
			schm.setPrtry(initialTrx.getDebtorAccountScheme());
			gen.setSchmeNm(schm);
			accId.setOthr(gen);
			dbtrAcc.setId(accId);
			ref.setDbtrAcct(dbtrAcc);
		}
		// creditor account
		if(initialTrx.getCreditorAccountId() != null) {
			CashAccount40 crdAcc = new CashAccount40();
			crdAcc.setCcy(initialTrx.getCreditorAccountCurrency());
			AccountIdentification4Choice accId = new AccountIdentification4Choice();
			GenericAccountIdentification1 gen = new GenericAccountIdentification1();
			gen.setId(initialTrx.getCreditorAccountId());
			AccountSchemeName1Choice schm = new AccountSchemeName1Choice();
			schm.setPrtry(initialTrx.getCreditorAccountScheme());
			gen.setSchmeNm(schm);
			accId.setOthr(gen);
			crdAcc.setId(accId);
			ref.setCdtrAcct(crdAcc);
		}
		//creditor
		return ref;
	}

	private StatusReasonInformation12 createStatusReason(PaymentExtended payment, String st) {
		StatusReasonInformation12 status = new StatusReasonInformation12();
		StatusReason6Choice reason = new StatusReason6Choice();
		LOG.trace("createStatusReason::converting reject code "+payment.getRejectCode());
		String code = paramStore.getRejectMapping().get(payment.getRejectCode());
		LOG.trace("createStatusReason::converted reject code "+code);
		if(StringUtil.isNullOrEmpty(code)) {
			code = paramStore.getDefaultRejectCode();
		}
		LOG.trace("createStatusReason::final converted reject code "+code);
		reason.setCd(code);
		status.setRsn(reason);
		String addInf = null;
		if("X050".equals(code)) {
			addInf = "No data found";
		}else {
			addInf = payment.getRejectMessage();
		}
		if(!StringUtil.isNullOrEmpty(addInf)) {
			addInf = addInf.replace("<", "'").replace(">", "'"); 
		}
		status.getAddtlInf().add(addInf);
		return status;
	}

}
