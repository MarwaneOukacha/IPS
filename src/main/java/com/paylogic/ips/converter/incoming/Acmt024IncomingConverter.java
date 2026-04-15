package com.paylogic.ips.converter.incoming;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gms.utils.common.StringUtil;
import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.ErrorStatus;
import com.gms.utils.net.webinterface.WebRequest;
import com.paylogic.ama.core.exception.RemoteEndPointException;
import com.paylogic.ama.core.model.AccountInfo;
import com.paylogic.ama.core.model.ParameterCategory;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ips.converter.DocumentConverter;
import com.paylogic.ips.iso20022.bo.acmt024.CashAccount40;
import com.paylogic.ips.iso20022.bo.acmt024.DocumentAcmt024;
import com.paylogic.ips.iso20022.bo.acmt024.FinancialInstitutionIdentification18;
import com.paylogic.ips.iso20022.bo.acmt024.IdentificationAssignment3;
import com.paylogic.ips.iso20022.bo.acmt024.IdentificationVerificationReportV03;
import com.paylogic.ips.iso20022.bo.acmt024.SupplementaryData1;
import com.paylogic.ips.iso20022.bo.acmt024.VerificationReason1Choice;
import com.paylogic.ips.iso20022.bo.acmt024.VerificationReport4;
import com.paylogic.ips.store.IsoParameterStore;
import com.paylogic.ips.util.CoreUtil;

@Component
public class Acmt024IncomingConverter extends DocumentConverter{
	@Autowired
	private IsoParameterStore paramStore;
	
	public Payment convertToPaymentResponse(DocumentAcmt024 document024,List<ParameterCategory> cats, Payment initialReqest) throws RemoteEndPointException {
		Payment payment = new Payment();
		IdentificationVerificationReportV03 acmt024 = document024.getIdVrfctnRpt();
		//process header
		convertHeader(acmt024.getAssgnmt(),cats,payment);
		//process verification info
		convertVerifInfo(acmt024.getRpt().get(0),cats,payment);
		//process any supp data
		convertSupplementaryData(acmt024.getSplmtryData(),cats,payment);
		//set iso20022 message in additional data
		includeMessage(document024,cats,payment,true);
		// set initial data
		payment.setVoucherCode(initialReqest.getVoucherCode());
		return payment;
	}	

	private void convertHeader(IdentificationAssignment3 header, List<ParameterCategory> cats, Payment payment) {
		// create time
		payment.setCreateTime(CoreUtil.getDateFromXmlGregorienDate(header.getCreDtTm()));
		// from member
		getMember(header.getAssgnr().getAgt().getFinInstnId(),cats,payment,true);
		// to member
		getMember(header.getAssgne().getAgt().getFinInstnId(),cats,payment,false);
		//intent
		payment.setIntent(CoreUtil.ACCOUNT_INQUIRY);
		//issuer trx ref
		payment.setAcquirerTrxRef(header.getMsgId());
	}
	
	private void convertVerifInfo(VerificationReport4 verifInf, List<ParameterCategory> cats, Payment payment) throws RemoteEndPointException {
		//state
		if(verifInf.isVrfctn()) {
			payment.setState(CoreUtil.PAY_STATUS_ACCEPTED);
		}else {
			payment.setState(CoreUtil.PAY_STATUS_REJECTED);
			//reject msg
			throwErrorException(verifInf.getRsn());
		}
		// set destination wallet/account
		CashAccount40 acc = null;
		if(verifInf.getOrgnlPtyAndAcctId() != null && verifInf.getOrgnlPtyAndAcctId().getAcct() != null) {
			acc = verifInf.getOrgnlPtyAndAcctId().getAcct();
		}else if(verifInf.getUpdtdPtyAndAcctId() != null && verifInf.getUpdtdPtyAndAcctId().getAcct() != null) {
			acc = verifInf.getUpdtdPtyAndAcctId().getAcct();
		}
		if(acc != null) {
			getAccountFromGenericAccountIdentification(acc,cats,payment);
		}
	}
	
	private void convertSupplementaryData(List<SupplementaryData1> splmtryData, List<ParameterCategory> cats, Payment payment) {
		//Nothing to do
	}
	
	private void getMember(FinancialInstitutionIdentification18 finInstnId, List<ParameterCategory> cats, Payment payment, boolean fromMember) {
		if(fromMember) {
			payment.setFromMember(finInstnId.getOthr().getId()); 
		}else {
			payment.setToMember(finInstnId.getOthr().getId()); 
		}
	}
	
	private void getAccountFromGenericAccountIdentification(CashAccount40 cashAcc, List<ParameterCategory> cats, Payment payment) {
		AccountInfo accInfo = new AccountInfo();
		if(cashAcc.getId() != null && cashAcc.getId().getOthr() != null) {
			accInfo.setIden(cashAcc.getId().getOthr().getId());
		}
		if(cashAcc.getPrxy() != null) {
			accInfo.setAlias(cashAcc.getPrxy().getId());
		}
		accInfo.setType(CoreUtil.ACC_INFO_ACCOUNT);
		accInfo.setCurrency(cashAcc.getCcy());
		accInfo.setName(cashAcc.getNm());
		if(cashAcc.getId() != null && cashAcc.getId().getOthr() != null && cashAcc.getId().getOthr().getSchmeNm() != null) {
			String type = cashAcc.getId().getOthr().getSchmeNm().getPrtry();
			accInfo.setType(CoreUtil.ISO22_ACCOUNT_TYPE.equals(type) ? CoreUtil.ISO22_ACCOUNT_TYPE : CoreUtil.ISO22_EWALLET_TYPE);
		}
		payment.setDstAccounts(new ArrayList<>());
		payment.getDstAccounts().add(accInfo);
	}
	
	private void throwErrorException(VerificationReason1Choice reason) throws RemoteEndPointException{
		String error = BusinessException.class.getSimpleName();
		String rejectMsg;
		if(reason != null) {
			rejectMsg = reason.getCd();
			if(!StringUtil.isNullOrEmpty(reason.getPrtry())) {
				rejectMsg += ":"+reason.getPrtry();
			}
			for(Entry<String, String> entry : paramStore.getRejectMapping().entrySet()) {
				if(entry.getValue().equals(reason.getCd())) {
					error = entry.getKey();
					break;
				}
			}
		}else {
			 rejectMsg = "Rejected with no additional information";
		}
		WebRequest request = new WebRequest();
		request.convertToJson(new ErrorStatus(error, rejectMsg));
		throw new RemoteEndPointException(400, request.convertToJson(new ErrorStatus(error, rejectMsg)));
	}
}
