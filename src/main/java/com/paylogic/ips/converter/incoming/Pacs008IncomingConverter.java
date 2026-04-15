package com.paylogic.ips.converter.incoming;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gms.utils.common.Pair;
import com.gms.utils.common.StringUtil;
import com.paylogic.ama.core.model.AccountInfo;
import com.paylogic.ama.core.model.CustomerKyc;
import com.paylogic.ama.core.model.ParameterCategory;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ama.core.utils.BaseCoreUtil.PAYMENT_ACTION;
import com.paylogic.ips.converter.DocumentConverter;
import com.paylogic.ips.iso20022.bo.pacs008.ActiveOrHistoricCurrencyAndAmount;
import com.paylogic.ips.iso20022.bo.pacs008.CreditTransferTransaction58;
import com.paylogic.ips.iso20022.bo.pacs008.DocumentPacs008;
import com.paylogic.ips.iso20022.bo.pacs008.FIToFICustomerCreditTransferV11;
import com.paylogic.ips.iso20022.bo.pacs008.FinancialInstitutionIdentification18;
import com.paylogic.ips.iso20022.bo.pacs008.GenericAccountIdentification1;
import com.paylogic.ips.iso20022.bo.pacs008.GroupHeader96;
import com.paylogic.ips.iso20022.bo.pacs008.PartyIdentification135;
import com.paylogic.ips.iso20022.bo.pacs008.SupplementaryData1;
import com.paylogic.ips.store.IsoParameterStore;
import com.paylogic.ips.util.CoreUtil;

@Component
public class Pacs008IncomingConverter extends DocumentConverter{
	@Autowired
	private IsoParameterStore handlerStore;
	
	public Payment convertPacs008(DocumentPacs008 document008,List<ParameterCategory> cats) {
		Payment payment = new Payment();
		payment.setAction(PAYMENT_ACTION.SEND);
		FIToFICustomerCreditTransferV11 pacs008 = document008.getFIToFICstmrCdtTrf();
		//process header
		convertHeader(pacs008.getGrpHdr(),cats,payment);
		//process credit info
		convertCreditInfo(pacs008.getCdtTrfTxInf().get(0),cats,payment);
		//process any supp data
		convertSupplementaryData(pacs008.getSplmtryData(),cats,payment);
		//set iso20022 message in additional data
		includeMessage(document008,cats,payment,true);
		return payment;
	}	

	private void convertHeader(GroupHeader96 header, List<ParameterCategory> cats, Payment payment) {
		// create time
		payment.setCreateTime(CoreUtil.getDateFromXmlGregorienDate(header.getCreDtTm()));
		// from member
		getMember(header.getInstgAgt().getFinInstnId(),cats,payment,true);
		// to member
		getMember(header.getInstdAgt().getFinInstnId(),cats,payment,false);
		//intent
		payment.setIntent(header.getPmtTpInf().getLclInstrm().getPrtry());
	}
	
	private void convertCreditInfo(CreditTransferTransaction58 cdtTrfTxInf, List<ParameterCategory> cats, Payment payment) {
		//Issuer trx ref
		payment.setIssuerTrxRef(cdtTrfTxInf.getPmtId().getTxId());
		// transaction amount and currency
		Pair<Double, String> amount = getPaymentAmount(cdtTrfTxInf.getInstdAmt(), handlerStore.getCurrencyMapping());
		payment.setAmount(amount.getLeft());
		payment.setCurrency(amount.getRight());
		// set Sender & Receiver KYC
		getCustomerKycFromPartyInfo(cdtTrfTxInf.getDbtr(), cats, true, payment);
		getCustomerKycFromPartyInfo(cdtTrfTxInf.getCdtr(), cats, false, payment);
		// set Sender and Receiver accounts
		getAccountFromGenericAccountIdentification(cdtTrfTxInf.getDbtrAcct().getId().getOthr(),cats,true,payment);
		getAccountFromGenericAccountIdentification(cdtTrfTxInf.getCdtrAcct().getId().getOthr(),cats,false,payment);
		//ignore DbtrAgt it is already set from header
		//ignore CdtrAgt it is already set from header
		//Transaction description
		if(cdtTrfTxInf.getRmtInf() != null && !cdtTrfTxInf.getRmtInf().getUstrd().isEmpty()) {
			payment.setDescription(StringUtil.implode(cdtTrfTxInf.getRmtInf().getUstrd(), ';'));
		}
	}
	
	private void convertSupplementaryData(List<SupplementaryData1> splmtryData, List<ParameterCategory> cats, Payment payment) {
		//Nothing to do
	}
	
	private Pair<Double, String> getPaymentAmount(ActiveOrHistoricCurrencyAndAmount achAmount, Map<String, String> currencyMap) {
		Pair<Double, String> amount = new Pair<>();
		amount.setLeft(achAmount.getValue().doubleValue());
		for(Entry<String, String> entry : currencyMap.entrySet()) {
			if(achAmount.getCcy().equals(entry.getValue())) {
				amount.setRight(entry.getKey());
				break;
			}
		}
		return amount;
	}
	
	private void getMember(FinancialInstitutionIdentification18 finInstnId, List<ParameterCategory> cats, Payment payment, boolean fromMember) {
		if(fromMember) {
			payment.setFromMember(finInstnId.getOthr().getId()); 
		}else {
			payment.setToMember(finInstnId.getOthr().getId()); 
		}
	}
	
	private void getCustomerKycFromPartyInfo(PartyIdentification135 partyIden, List<ParameterCategory> cats,boolean sender,Payment payment) {
		CustomerKyc kyc = new CustomerKyc();
		String name = partyIden.getNm();
		if(!StringUtil.isNullOrEmpty(name)) {
			String fmt = CoreUtil.extractValuefromEpParam(cats, ISO22_KYC, NAME_COMPOSITION,DEFAULT_NAME_COMPOSITION);
			int indexFirst  = fmt.indexOf("%f");
			int indexMiddle = fmt.indexOf("%m");
			int indexSecond = fmt.indexOf("%s");
			String sep = CoreUtil.extractValuefromEpParam(cats, ISO22_KYC, NAME_SEPARATOR,DEFAULT_NAME_SEPARATOR);
			String[] names = name.split(sep);
			if(indexFirst >= 0 && indexFirst < names.length) {
				kyc.setFirstname(names[indexFirst]);
			}
			if(indexSecond >= 0 && indexSecond < names.length) {
				kyc.setSecondname(names[indexSecond]);
			}
			if(indexMiddle >= 0 && indexMiddle < names.length) {
				kyc.setMiddlename(names[indexMiddle]);
			}
		}
		if(partyIden.getPstlAdr() != null) {
			kyc.setCity(partyIden.getPstlAdr().getTwnNm());
			kyc.setCountry(partyIden.getPstlAdr().getCtry());
			if(!partyIden.getPstlAdr().getAdrLine().isEmpty()) {
				kyc.setAddress(StringUtil.implode(partyIden.getPstlAdr().getAdrLine(), '\n'));
			}
		}
		if(sender) {
			payment.setSenderCustomerData(kyc);
		}else {
			payment.setReceiverCustomerData(kyc);
		}
	}
	
	private void getAccountFromGenericAccountIdentification(GenericAccountIdentification1  othr, List<ParameterCategory> cats, boolean sender, Payment payment) {
		String type = othr.getSchmeNm().getPrtry();
		if(CoreUtil.ISO22_ACCOUNT_TYPE.equals(type)) {
			AccountInfo accInfo = null;
			accInfo = new AccountInfo();
			accInfo.setIden(othr.getId());
			accInfo.setType(CoreUtil.ACC_INFO_ACCOUNT);
			if(sender) {
				payment.setSrcAccounts(new ArrayList<>());
				payment.getSrcAccounts().add(accInfo);
			}else {
				payment.setDstAccounts(new ArrayList<>());
				payment.getDstAccounts().add(accInfo);
			}
		}else if(CoreUtil.ISO22_EWALLET_TYPE.equals(type)) {
			if(sender) {
				payment.setWalletSource(othr.getId());
			}else {
				payment.setWalletDestination(othr.getId());
			}
		}
		else {
			AccountInfo accInfo = new AccountInfo();
			accInfo.setIden(othr.getId());
			accInfo.setType(CoreUtil.ACC_INFO_CARD);
			if(sender) {
				payment.setSrcAccounts(new ArrayList<AccountInfo>());
				payment.getSrcAccounts().add(accInfo);
			}
			else {
				payment.setDstAccounts(new ArrayList<AccountInfo>());
				payment.getDstAccounts().add(accInfo);
			}
		}
	}
}
