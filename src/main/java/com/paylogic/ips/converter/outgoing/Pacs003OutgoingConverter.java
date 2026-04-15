package com.paylogic.ips.converter.outgoing;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gms.utils.common.Pair;
import com.gms.utils.common.StringUtil;
import com.paylogic.ama.core.model.CustomerKyc;
import com.paylogic.ama.core.model.ParameterCategory;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ips.converter.DocumentConverter;
import com.paylogic.ips.iso20022.bo.pacs003.AccountIdentification4Choice;
import com.paylogic.ips.iso20022.bo.pacs003.AccountSchemeName1Choice;
import com.paylogic.ips.iso20022.bo.pacs003.ActiveOrHistoricCurrencyAndAmount;
import com.paylogic.ips.iso20022.bo.pacs003.BranchAndFinancialInstitutionIdentification6;
import com.paylogic.ips.iso20022.bo.pacs003.CashAccount40;
import com.paylogic.ips.iso20022.bo.pacs003.ChargeBearerType1Code;
import com.paylogic.ips.iso20022.bo.pacs003.DirectDebitTransactionInformation30;
import com.paylogic.ips.iso20022.bo.pacs003.DocumentPacs003;
import com.paylogic.ips.iso20022.bo.pacs003.FIToFICustomerDirectDebitV10;
import com.paylogic.ips.iso20022.bo.pacs003.FinancialInstitutionIdentification18;
import com.paylogic.ips.iso20022.bo.pacs003.GenericAccountIdentification1;
import com.paylogic.ips.iso20022.bo.pacs003.GenericFinancialIdentification1;
import com.paylogic.ips.iso20022.bo.pacs003.GroupHeader98;
import com.paylogic.ips.iso20022.bo.pacs003.LocalInstrument2Choice;
import com.paylogic.ips.iso20022.bo.pacs003.PartyIdentification135;
import com.paylogic.ips.iso20022.bo.pacs003.PaymentIdentification13;
import com.paylogic.ips.iso20022.bo.pacs003.PaymentTypeInformation27;
import com.paylogic.ips.iso20022.bo.pacs003.PostalAddress24;
import com.paylogic.ips.iso20022.bo.pacs003.RemittanceInformation21;
import com.paylogic.ips.iso20022.bo.pacs003.SettlementInstruction14;
import com.paylogic.ips.iso20022.bo.pacs003.SettlementMethod2Code;
import com.paylogic.ips.store.IsoParameterStore;
import com.paylogic.ips.util.CoreUtil;

@Component
public class Pacs003OutgoingConverter extends DocumentConverter {

    private static final Logger LOG = Logger.getLogger(Pacs003OutgoingConverter.class);
    private static final String ISO22_SETTLEMENT_CAT = "ISO22_SETTLEMENT_CAT";
	private static final String SETTLEMENT_METHOD = "SETTLEMENT_METHOD";
	private static final String DEFAULT_SETTLEMENT_METHODE = "CLRG";
    private static final String ISO22_KYC = "ISO22_KYC";
    private static final String NAME_COMPOSITION = "NAME_COMPOSITION";
    private static final String DEFAULT_NAME_COMPOSITION = "%f %m %s";

    @Autowired
    private IsoParameterStore handlerStore;

    
    public DocumentPacs003 convertPayment(Payment payment, List<ParameterCategory> cats) {
        // Create the direct debit message container
        FIToFICustomerDirectDebitV10 pacs003 = new FIToFICustomerDirectDebitV10();
        // Build and set Group Header
        pacs003.setGrpHdr(createHeader(payment, cats));
        // Build and add the Direct Debit Transaction Information
        pacs003.getDrctDbtTxInf().add(createDirectDebitTransaction(payment, cats));
        // Wrap in Document and include the raw message if needed
        DocumentPacs003 document = new DocumentPacs003();
        document.setFIToFICstmrDrctDbt(pacs003);
        includeMessage(document, cats, payment, false);
        return document;
    }

    
    private GroupHeader98 createHeader(Payment payment, List<ParameterCategory> cats) {
        GroupHeader98 header = new GroupHeader98();
        // Message ID and creation time
        header.setMsgId(payment.getIssuerTrxRef());
        try {
            XMLGregorianCalendar xmlDate = CoreUtil.convertDateToXmlGregorienDate(payment.getCreateTime(), true);
            header.setCreDtTm(xmlDate);
        } catch (DatatypeConfigurationException e) {
            LOG.warn("Failed to convert payment createTime to XMLGregorianCalendar", e);
            header.setCreDtTm(null);
        }
        header.setNbOfTxs("1");
        // set Settlement
     	header.setSttlmInf(createSettlementInstruction(payment, cats));
        // Payment type information (local instrument code)
        header.setPmtTpInf(createPaymentTypeInfo(payment, cats));
        // Instructing and instructed agents (fromMember and toMember)
        header.setInstgAgt(createFinancialInst(payment, cats, true));
        header.setInstdAgt(createFinancialInst(payment, cats, false));
        return header;
    }

    
    private DirectDebitTransactionInformation30 createDirectDebitTransaction(Payment payment, List<ParameterCategory> cats) {
        DirectDebitTransactionInformation30 txInfo = new DirectDebitTransactionInformation30();
        // Payment identification (transaction ID, and optional clearing system reference)
        txInfo.setPmtId(createPaymentIdentification(payment));
        // Instructed amount (value and currency)
        txInfo.setInstdAmt(createCurrencyAmount(payment.getAmount(), payment.getCurrency(), handlerStore.getCurrencyMapping()));
        // Charge bearer (shared by default)
        txInfo.setChrgBr(ChargeBearerType1Code.SLEV);
        // Debtor (the payer's information) and Debtor Account
        txInfo.setDbtr(createPartyIdentification(payment, cats, true));
        txInfo.setDbtrAcct(createCashAccount(payment, cats, true));
        // Debtor Agent (payer's bank) and Creditor Agent (payee's bank)
        txInfo.setDbtrAgt(createFinancialInst(payment, cats, false));
        txInfo.setCdtrAgt(createFinancialInst(payment, cats, true));
        // Creditor (the payee's information) and Creditor Account
        txInfo.setCdtr(createPartyIdentification(payment, cats, false));
        txInfo.setCdtrAcct(createCashAccount(payment, cats, false));
        // Remittance information (unstructured text description)
        if (!StringUtil.isNullOrEmpty(payment.getDescription())) {
            RemittanceInformation21 rmtInfo = new RemittanceInformation21();
            rmtInfo.getUstrd().add(payment.getDescription());
            txInfo.setRmtInf(rmtInfo);
        }
        return txInfo;
    }
    
    private SettlementInstruction14 createSettlementInstruction(Payment payment, List<ParameterCategory> cats) {
		// set Settlement method
		SettlementInstruction14 setlmnt = new SettlementInstruction14();
		SettlementMethod2Code method = SettlementMethod2Code.CLRG;
		String value = CoreUtil.extractValuefromEpParam(cats, ISO22_SETTLEMENT_CAT, SETTLEMENT_METHOD,DEFAULT_SETTLEMENT_METHODE);
		method = SettlementMethod2Code.fromValue(value);
		LOG.trace("createSettlementInstruction::settlement method is "+method);
		setlmnt.setSttlmMtd(method);
		return setlmnt;		
	}

    
    private PaymentTypeInformation27 createPaymentTypeInfo(Payment payment, List<ParameterCategory> cats) {
        PaymentTypeInformation27 pmtType = new PaymentTypeInformation27();
        LocalInstrument2Choice localInstr = new LocalInstrument2Choice();
        localInstr.setPrtry(payment.getIntent());
        pmtType.setLclInstrm(localInstr);
        return pmtType;
    }

    
    private PaymentIdentification13 createPaymentIdentification(Payment payment) {
        PaymentIdentification13 pmtId = new PaymentIdentification13();
        pmtId.setTxId(payment.getIssuerTrxRef());
        if (!StringUtil.isNullOrEmpty(payment.getVoucherCode())) {
            pmtId.setClrSysRef(payment.getVoucherCode());
        }
        return pmtId;
    }

    
    private BranchAndFinancialInstitutionIdentification6 createFinancialInst(Payment payment, List<ParameterCategory> cats, boolean useFromMember) {
        BranchAndFinancialInstitutionIdentification6 inst = new BranchAndFinancialInstitutionIdentification6();
        FinancialInstitutionIdentification18 finInstId = new FinancialInstitutionIdentification18();
        GenericFinancialIdentification1 genericId = new GenericFinancialIdentification1();
        // Use the appropriate member ID
        genericId.setId(useFromMember ? payment.getFromMember() : payment.getToMember());
        finInstId.setOthr(genericId);
        inst.setFinInstnId(finInstId);
        return inst;
    }

    
    private ActiveOrHistoricCurrencyAndAmount createCurrencyAmount(Double amount, String currency, Map<String, String> currencyMap) {
        ActiveOrHistoricCurrencyAndAmount isoAmount = new ActiveOrHistoricCurrencyAndAmount();
        isoAmount.setValue(BigDecimal.valueOf(amount));
        // Map internal currency code to actual ISO currency code
        isoAmount.setCcy(currencyMap.get(currency));
        return isoAmount;
    }

    
    private CashAccount40 createCashAccount(Payment payment, List<ParameterCategory> cats, boolean isSource) {
        CashAccount40 cashAcc = new CashAccount40();
        AccountIdentification4Choice acctIdChoice = new AccountIdentification4Choice();
        acctIdChoice.setOthr(getGenericAccountId(payment, cats, isSource));
        cashAcc.setId(acctIdChoice);
        return cashAcc;
    }

    
    private GenericAccountIdentification1 getGenericAccountId(Payment payment, List<ParameterCategory> cats, boolean isSource) {
        GenericAccountIdentification1 acct = new GenericAccountIdentification1();
        Pair<String, String> accInfo = CoreUtil.getPaymentAccountId(payment, cats, isSource);
        // Set account or wallet identifier
        acct.setId(accInfo.getLeft());
        // Set the scheme name (proprietary code for account type)
        AccountSchemeName1Choice schemeName = new AccountSchemeName1Choice();
        schemeName.setPrtry(accInfo.getRight());
        acct.setSchmeNm(schemeName);
        return acct;
    }

    
    private PartyIdentification135 createPartyIdentification(Payment payment, List<ParameterCategory> cats, boolean isSender) {
        PartyIdentification135 party = new PartyIdentification135();
        CustomerKyc kyc = isSender ? payment.getSenderCustomerData() : payment.getReceiverCustomerData();
        if (kyc != null) {
            // Set the full name from KYC (formatted per configuration)
            party.setNm(formatNameFromKyc(kyc, cats));
            // Set the address if available
            if (!StringUtil.isNullOrEmpty(kyc.getAddress()) ||
                !StringUtil.isNullOrEmpty(kyc.getCity()) ||
                !StringUtil.isNullOrEmpty(kyc.getCountry())) {
                PostalAddress24 address = new PostalAddress24();
                address.setCtry(kyc.getCountry());
                address.setTwnNm(kyc.getCity());
                address.getAdrLine().add(kyc.getAddress());
                party.setPstlAdr(address);
            }
        }
        return party;
    }

    
    private String formatNameFromKyc(CustomerKyc customerData, List<ParameterCategory> cats) {
        String format = CoreUtil.extractValuefromEpParam(cats, ISO22_KYC, NAME_COMPOSITION, DEFAULT_NAME_COMPOSITION);
        // Replace placeholders with actual name parts (or empty if null)
        format = format.replace("%f", StringUtil.isNullOrEmpty(customerData.getFirstname()) ? "" : customerData.getFirstname());
        format = format.replace("%m", StringUtil.isNullOrEmpty(customerData.getMiddlename()) ? "" : customerData.getMiddlename());
        format = format.replace("%s", StringUtil.isNullOrEmpty(customerData.getSecondname()) ? "" : customerData.getSecondname());
        // Return the assembled name (or null if the result is empty string)
        return StringUtil.nullIfEmpty(format);
    }
}
