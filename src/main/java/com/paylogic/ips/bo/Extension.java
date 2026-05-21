package com.paylogic.ips.bo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class Extension {
	private Ttl ttl;
	private String creditorName;

	private CreditorAccount creditorAccount;
	private CreditorAgent creditorAgent;
	private Amount amount;
	private String e2e;
	private String DBA;
	private String mcc;
	private String bankOpCode;
	private String ttc;
	private String remittanceInfo4Payer;
	private String creditorRef;
	private AdditionalInfo additionalInfo;
	private Amount amountMin;
	private Amount amountMax;

	public String getDBA() {
		return DBA;
	}

	public void setDBA(String dBA) {
		DBA = dBA;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public String getRemittanceInfo4Payer() {
		return remittanceInfo4Payer;
	}

	public void setRemittanceInfo4Payer(String remittanceInfo4Payer) {
		this.remittanceInfo4Payer = remittanceInfo4Payer;
	}

	public String getCreditorRef() {
		return creditorRef;
	}

	public void setCreditorRef(String creditorRef) {
		this.creditorRef = creditorRef;
	}

	public AdditionalInfo getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(AdditionalInfo additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public Amount getAmountMin() {
		return amountMin;
	}

	public void setAmountMin(Amount amountMin) {
		this.amountMin = amountMin;
	}

	public Amount getAmountMax() {
		return amountMax;
	}

	public void setAmountMax(Amount amountMax) {
		this.amountMax = amountMax;
	}

	public Ttl getTtl() {
		return ttl;
	}

	public void setTtl(Ttl ttl) {
		this.ttl = ttl;
	}

	public String getCreditorName() {
		return creditorName;
	}

	public void setCreditorName(String creditorName) {
		this.creditorName = creditorName;
	}

	public CreditorAccount getCreditorAccount() {
		return creditorAccount;
	}

	public void setCreditorAccount(CreditorAccount creditorAccount) {
		this.creditorAccount = creditorAccount;
	}

	public CreditorAgent getCreditorAgent() {
		return creditorAgent;
	}

	public void setCreditorAgent(CreditorAgent creditorAgent) {
		this.creditorAgent = creditorAgent;
	}

	public Amount getAmount() {
		return amount;
	}

	public void setAmount(Amount amount) {
		this.amount = amount;
	}

	public String getE2e() {
		return e2e;
	}

	public void setE2e(String e2e) {
		this.e2e = e2e;
	}

	public String getBankOpCode() {
		return bankOpCode;
	}

	public void setBankOpCode(String bankOpCode) {
		this.bankOpCode = bankOpCode;
	}

	public String getTtc() {
		return ttc;
	}

	public void setTtc(String ttc) {
		this.ttc = ttc;
	}

	@Override
	public String toString() {
		return "Extension [ttl=" + ttl + ", creditorName=" + creditorName + ", creditorAccount=" + creditorAccount
				+ ", creditorAgent=" + creditorAgent + ", amount=" + amount + ", e2e=" + e2e + ", DBA=" + DBA + ", mcc="
				+ mcc + ", bankOpCode=" + bankOpCode + ", ttc=" + ttc + ", remittanceInfo4Payer=" + remittanceInfo4Payer
				+ ", creditorRef=" + creditorRef + ", additionalInfo=" + additionalInfo + ", amountMin=" + amountMin
				+ ", amountMax=" + amountMax + "]";
	}

}