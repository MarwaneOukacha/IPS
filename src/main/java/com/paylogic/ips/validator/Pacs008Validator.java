package com.paylogic.ips.validator;

import java.util.List;

import org.apache.log4j.Logger;

import com.gms.utils.common.StringUtil;
import com.gms.utils.exception.BusinessException;
import com.gms.utils.exception.InvalidValueEx;
import com.gms.utils.exception.MandatoryFieldEx;
import com.paylogic.ama.core.exception.InvalidFromMemberEx;
import com.paylogic.ama.core.exception.InvalidToMemberEx;
import com.paylogic.ama.core.exception.InvalidTransactionEx;
import com.paylogic.ips.iso20022.bo.pacs008.CashAccount40;
import com.paylogic.ips.iso20022.bo.pacs008.ChargeBearerType1Code;
import com.paylogic.ips.iso20022.bo.pacs008.CreditTransferTransaction58;
import com.paylogic.ips.iso20022.bo.pacs008.FIToFICustomerCreditTransferV11;
import com.paylogic.ips.iso20022.bo.pacs008.GroupHeader96;
import com.paylogic.ips.iso20022.bo.pacs008.PartyIdentification135;
import com.paylogic.ips.iso20022.bo.pacs008.SettlementMethod1Code;
import com.paylogic.ips.util.CoreUtil;


public class Pacs008Validator {
	private static final Logger LOG = Logger.getLogger(Pacs008Validator.class.getName());
	
	public static void validate(FIToFICustomerCreditTransferV11 incoming,boolean kycMandatory) throws BusinessException {
		//Header
		validateHeader(incoming.getGrpHdr());
		//Credit Info
		List<CreditTransferTransaction58> credits = incoming.getCdtTrfTxInf();
		if(credits == null) {
			LOG.info("validateRequest::Credit transfer Transactions is empty");
			throw new MandatoryFieldEx("CdtTrfTxInf");
		}
		if(credits.size() != 1) {
			LOG.info("validateRequest::Credit transfer Transactions size is different from 1");
			throw new InvalidValueEx("CdtTrfTxInf", "1", credits.size());
		}
		//MsgId must be equal to transaction ref
		if(!incoming.getGrpHdr().getMsgId().equals(credits.get(0).getPmtId().getTxId())) {
			LOG.info("validateRequest::Message ID must be equal to transaction id");
			throw new InvalidValueEx("TxId", incoming.getGrpHdr().getMsgId(), credits.get(0).getPmtId().getTxId());
		}
		//Payment type
		validateIntent(incoming.getGrpHdr());
		//Charge bearer
		if(!ChargeBearerType1Code.SLEV.equals(credits.get(0).getChrgBr())) {
			LOG.info("validateRequest::Invalid charge bearer value <"+credits.get(0).getChrgBr()+">");
			throw new InvalidValueEx("ChrgBr", ChargeBearerType1Code.SLEV, credits.get(0).getChrgBr());
		}
		//Amount and currency
		validateAmount(credits.get(0));
		//Validate Kyc if needed
		if(kycMandatory) {
			//validate source customer KYC
			validateKyc(credits.get(0).getDbtr(), true);
			//validate destination customer KYC
			validateKyc(credits.get(0).getCdtr(), false);
		}
		//validate source account/wallet
		validateAccount(credits.get(0).getDbtrAcct(), true);
		//validate destination account/wallet
		validateAccount(credits.get(0).getCdtrAcct(), false);
	}

	private static void validateAmount(CreditTransferTransaction58 credit) throws BusinessException {
		// transaction amount and currency
		if(credit.getInstdAmt() == null) {
			LOG.info("validateRequest::Credit transfer amount is missing");
			throw new MandatoryFieldEx("InstdAmt");
		}
		if(credit.getInstdAmt().getValue() == null) {
			LOG.info("validateRequest::Credit transfer amount is missing");
			throw new MandatoryFieldEx("InstdAmt");
		}
		if(StringUtil.isNullOrEmpty(credit.getInstdAmt().getCcy())) {
			LOG.info("validateRequest::Credit transfer currency is missing");
			throw new MandatoryFieldEx("InstdAmt");
		}
	}

	private static void validateIntent(GroupHeader96 header) throws BusinessException {
		if(header.getPmtTpInf() == null || header.getPmtTpInf().getLclInstrm() == null || StringUtil.isNullOrEmpty(header.getPmtTpInf().getLclInstrm().getPrtry())) {
			LOG.info("validateRequest::Payment information is missing");
			throw new InvalidTransactionEx("PmtTpInf.LclInstrm.Prtry", CoreUtil.PACS008_VALID_INTENT, header.getPmtTpInf().getLclInstrm().getPrtry());
		}
		if(!CoreUtil.PACS008_VALID_INTENT.contains(header.getPmtTpInf().getLclInstrm().getPrtry())) {
			LOG.info("validateRequest::Invalid intent for pacs008 <"+header.getPmtTpInf().getLclInstrm().getPrtry()+">");
			throw new InvalidTransactionEx("PmtTpInf.LclInstrm.Prtry", CoreUtil.PACS008_VALID_INTENT, header.getPmtTpInf().getLclInstrm().getPrtry());
		}
	}

	private static void validateHeader(GroupHeader96 header) throws BusinessException {
		if(header == null) {
			LOG.info("validateRequest::header is missing");
			throw new MandatoryFieldEx("GrpHdr");
		}
		//Message Id
		if(StringUtil.isNullOrEmpty(header.getMsgId())) {
			LOG.info("validateRequest::message id is missing");
			throw new MandatoryFieldEx("MsgId");
		}
		//create date and time
		if(header.getCreDtTm() == null) {
			LOG.info("validateRequest::creation date and time is missing");
			throw new MandatoryFieldEx("CreDtTm");
		}
		//settlement method
		if(!SettlementMethod1Code.CLRG.equals(header.getSttlmInf().getSttlmMtd())) {
			LOG.info("validateRequest::Settlement method <"+header.getSttlmInf().getSttlmMtd()+"> must be <"+SettlementMethod1Code.CLRG+">");
			throw new InvalidValueEx("SttlmInf", SettlementMethod1Code.CLRG, header.getSttlmInf().getSttlmMtd());
		}
		//from member
		if(header.getInstgAgt() == null 
				|| header.getInstgAgt().getFinInstnId() == null 
				|| header.getInstgAgt().getFinInstnId().getOthr() == null 
				|| StringUtil.isNullOrEmpty(header.getInstgAgt().getFinInstnId().getOthr().getId())) {
			LOG.info("validateRequest::missing from member");
			throw new InvalidFromMemberEx("null");
		}
		//to member
		if(header.getInstdAgt() == null 
			|| header.getInstdAgt().getFinInstnId() == null 
			|| header.getInstdAgt().getFinInstnId().getOthr() == null 
			|| StringUtil.isNullOrEmpty(header.getInstdAgt().getFinInstnId().getOthr().getId())) {
			LOG.info("validateRequest::missing to member");
			throw new InvalidToMemberEx("null");
		}
		//number of transactions
		if(!"1".equals(header.getNbOfTxs())) {
			LOG.info("validateRequest::Number of transactions must be 1");
			throw new InvalidValueEx("nbOfTxs", "1", header.getNbOfTxs());
		}
	}
	
	private static void validateAccount(CashAccount40 acc,boolean from) throws BusinessException {
		String strFrom = from ? "Debtor" : "Creditor";
		String field = from ? "Dbtr" : "Cdtr";
		if(acc == null) {
			LOG.info("validateRequest::"+strFrom+" is missing");
			throw new MandatoryFieldEx(field+"Acct");
		}
		if(acc.getId() == null || 
		   acc.getId().getOthr() == null ||
		   StringUtil.isNullOrEmpty(acc.getId().getOthr().getId())) {
			LOG.info("validateRequest::"+strFrom+" Account Id is missing");
			throw new MandatoryFieldEx(field+"Acct");
		}
		if(acc.getId().getOthr().getSchmeNm() == null ||
			StringUtil.isNullOrEmpty(acc.getId().getOthr().getSchmeNm().getPrtry())) {
			LOG.info("validateRequest::"+strFrom+" Account Type is missing");
			throw new MandatoryFieldEx("SchmeNm");
		}
		if(!CoreUtil.VALID_ACC_TYPE.contains(acc.getId().getOthr().getSchmeNm().getPrtry())) {
			LOG.info("validateRequest::"+strFrom+" Account Type is invalid "+acc.getId().getOthr().getSchmeNm().getPrtry());
			throw new InvalidValueEx("SchmeNm", CoreUtil.VALID_ACC_TYPE, acc.getId().getOthr().getSchmeNm().getPrtry());
		}
	}
	
	private static void validateKyc(PartyIdentification135 partyIden, boolean from) throws BusinessException {
		String strFrom = from ? "Debtor" : "Creditor";
		String field = from ? "Dbtr" : "Cdtr";
		if(partyIden == null) {
			LOG.info("validateRequest::"+strFrom+" is missing");
			throw new MandatoryFieldEx(field);
		}
		if(StringUtil.isNullOrEmpty(partyIden.getNm())) {
			LOG.info("validateRequest::"+strFrom+" Name is missing");
			throw new MandatoryFieldEx(field+".Nm");
			
		}
	}
}
