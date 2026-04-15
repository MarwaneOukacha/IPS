package com.paylogic.ips.converter.incoming;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.paylogic.ama.core.model.AccountInfo;
import com.paylogic.ama.core.model.ParameterCategory;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ama.core.utils.BaseCoreUtil.PAYMENT_ACTION;
import com.paylogic.ips.converter.DocumentConverter;
import com.paylogic.ips.iso20022.bo.acmt23.DocumentAcmt023;
import com.paylogic.ips.iso20022.bo.acmt23.FinancialInstitutionIdentification18;
import com.paylogic.ips.iso20022.bo.acmt23.GenericAccountIdentification1;
import com.paylogic.ips.iso20022.bo.acmt23.IdentificationAssignment3;
import com.paylogic.ips.iso20022.bo.acmt23.IdentificationVerification4;
import com.paylogic.ips.iso20022.bo.acmt23.IdentificationVerificationRequestV03;
import com.paylogic.ips.iso20022.bo.acmt23.SupplementaryData1;
import com.paylogic.ips.util.CoreUtil;

@Component
public class Acmt023IncomingConverter extends DocumentConverter{
	
	public Payment convertAcmt023(DocumentAcmt023 document023,List<ParameterCategory> cats) {
		Payment payment = new Payment();
		payment.setAction(PAYMENT_ACTION.SEND);
		IdentificationVerificationRequestV03 acmt023 = document023.getIdVrfctnReq();
		//process header
		convertHeader(acmt023.getAssgnmt(),cats,payment);
		//process verification info
		convertVerifInfo(acmt023.getVrfctn().get(0),cats,payment);
		//process any supp data
		convertSupplementaryData(acmt023.getSplmtryData(),cats,payment);
		//set iso20022 message in additional data
		includeMessage(document023,cats,payment,true);
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
		payment.setIssuerTrxRef(header.getMsgId());
	}
	
	private void convertVerifInfo(IdentificationVerification4 verifInf, List<ParameterCategory> cats, Payment payment) {
		// set destination wallet/account
		getAccountFromGenericAccountIdentification(verifInf.getPtyAndAcctId().getAcct().getId().getOthr(),cats,payment);
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
	
	private void getAccountFromGenericAccountIdentification(GenericAccountIdentification1 othr, List<ParameterCategory> cats, Payment payment) {
		String type = othr.getSchmeNm().getPrtry();
		if(CoreUtil.ISO22_ACCOUNT_TYPE.equals(type)) {
			AccountInfo accInfo = null;
			accInfo = new AccountInfo();
			accInfo.setIden(othr.getId());
			accInfo.setType(CoreUtil.ACC_INFO_ACCOUNT);
			payment.setDstAccounts(new ArrayList<>());
			payment.getDstAccounts().add(accInfo);
		}else {
			payment.setWalletDestination(othr.getId());
		}
	}
}
