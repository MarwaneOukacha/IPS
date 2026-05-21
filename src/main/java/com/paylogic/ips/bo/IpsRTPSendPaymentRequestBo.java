package com.paylogic.ips.bo;

public class IpsRTPSendPaymentRequestBo {
	private String traceReference;
	private String documentToken;
	private String uetr;
	private String debtorAccount;
	private String debtorName;
	private String creditorAccount;
	private String creditorName;
	private String receverBic;
	private PaymentRTPRequestBo paymentRTPRequestBo;
	
	
	public String getReceverBic() {
		return receverBic;
	}
	public void setReceverBic(String receverBic) {
		this.receverBic = receverBic;
	}
	public String getDebtorName() {
		return debtorName;
	}
	public void setDebtorName(String debtorName) {
		this.debtorName = debtorName;
	}
	public String getCreditorName() {
		return creditorName;
	}
	public void setCreditorName(String creditorName) {
		this.creditorName = creditorName;
	}
	public String getTraceReference() {
		return traceReference;
	}
	public void setTraceReference(String traceReference) {
		this.traceReference = traceReference;
	}
	public String getDocumentToken() {
		return documentToken;
	}
	public void setDocumentToken(String documentToken) {
		this.documentToken = documentToken;
	}
	public String getUetr() {
		return uetr;
	}
	public void setUetr(String uetr) {
		this.uetr = uetr;
	}
	public String getDebtorAccount() {
		return debtorAccount;
	}
	public void setDebtorAccount(String debtorAccount) {
		this.debtorAccount = debtorAccount;
	}
	public String getCreditorAccount() {
		return creditorAccount;
	}
	public void setCreditorAccount(String creditorAccount) {
		this.creditorAccount = creditorAccount;
	}
	public PaymentRTPRequestBo getPaymentRTPRequestBo() {
		return paymentRTPRequestBo;
	}
	public void setPaymentRTPRequestBo(PaymentRTPRequestBo paymentRTPRequestBo) {
		this.paymentRTPRequestBo = paymentRTPRequestBo;
	}
	@Override
	public String toString() {
		return "IpsRTPSendPaymentRequestBo [traceReference=" + traceReference + ", documentToken=" + documentToken
				+ ", uetr=" + uetr + ", debtorAccount=" + debtorAccount + ", debtorName=" + debtorName
				+ ", creditorAccount=" + creditorAccount + ", creditorName=" + creditorName + ", receverBic="
				+ receverBic + ", paymentRTPRequestBo=" + paymentRTPRequestBo + "]";
	}

	

}
