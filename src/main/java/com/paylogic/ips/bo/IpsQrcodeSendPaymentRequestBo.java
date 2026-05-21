package com.paylogic.ips.bo;

public class IpsQrcodeSendPaymentRequestBo {
	private PaymentQrcodeRequestBo paymentQrcodeRequestBo;
	private String e2e;
	private String qrExtensionUUID;
	private String traceReference;
	private String documentToken;
	private String uetr;
	private String debtorAccount;
	private String debtorName;
	private String creditorAccount;
	private String creditorName;
	private String receverBic;
	
	
	public String getReceverBic() {
		return receverBic;
	}


	public void setReceverBic(String receverBic) {
		this.receverBic = receverBic;
	}


	public PaymentQrcodeRequestBo getPaymentQrcodeRequestBo() {
		return paymentQrcodeRequestBo;
	}
	
	
	public String getQrExtensionUUID() {
		return qrExtensionUUID;
	}


	public void setQrExtensionUUID(String qrExtensionUUID) {
		this.qrExtensionUUID = qrExtensionUUID;
	}


	public void setPaymentQrcodeRequestBo(PaymentQrcodeRequestBo paymentQrcodeRequestBo) {
		this.paymentQrcodeRequestBo = paymentQrcodeRequestBo;
	}
	public String getE2e() {
		return e2e;
	}
	public void setE2e(String e2e) {
		this.e2e = e2e;
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
	public String getDebtorName() {
		return debtorName;
	}
	public void setDebtorName(String debtorName) {
		this.debtorName = debtorName;
	}
	public String getCreditorAccount() {
		return creditorAccount;
	}
	public void setCreditorAccount(String creditorAccount) {
		this.creditorAccount = creditorAccount;
	}
	public String getCreditorName() {
		return creditorName;
	}
	public void setCreditorName(String creditorName) {
		this.creditorName = creditorName;
	}
	@Override
	public String toString() {
		return "IpsQrcodeSendPaymentRequestBo [paymentQrcodeRequestBo=" + paymentQrcodeRequestBo + ", e2e=" + e2e
				+ ", traceReference=" + traceReference + ", documentToken=" + documentToken + ", uetr=" + uetr
				+ ", debtorAccount=" + debtorAccount + ", debtorName=" + debtorName + ", creditorAccount="
				+ creditorAccount + ", creditorName=" + creditorName + "]";
	}

	
}
