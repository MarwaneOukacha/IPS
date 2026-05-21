package com.paylogic.ips.bo;
public class IpsReturnPaymentRequestBo {

    private String traceReference;
    private String originalPaymentReference;
    private String originalPaymentValueDate;
    private String requestedAmount;
    private String originalDebtorBic;
    private String originalTransactionReference;
    private String debtorName;
    private String debtorAccount;
    private String creditorName;
    private String creditorAccount;
    private String creditorBic;
    private String purposeCode;
    private String remittanceInfo;
    
    
    
    public String getDebtorName() {
		return debtorName;
	}

	public void setDebtorName(String debtorName) {
		this.debtorName = debtorName;
	}

	public String getDebtorAccount() {
		return debtorAccount;
	}

	public void setDebtorAccount(String debtorAccount) {
		this.debtorAccount = debtorAccount;
	}

	public String getCreditorName() {
		return creditorName;
	}

	public void setCreditorName(String creditorName) {
		this.creditorName = creditorName;
	}

	public String getCreditorAccount() {
		return creditorAccount;
	}

	public void setCreditorAccount(String creditorAccount) {
		this.creditorAccount = creditorAccount;
	}

	public String getCreditorBic() {
		return creditorBic;
	}

	public void setCreditorBic(String creditorBic) {
		this.creditorBic = creditorBic;
	}

	public String getPurposeCode() {
		return purposeCode;
	}

	public void setPurposeCode(String purposeCode) {
		this.purposeCode = purposeCode;
	}

	public String getRemittanceInfo() {
		return remittanceInfo;
	}

	public void setRemittanceInfo(String remittanceInfo) {
		this.remittanceInfo = remittanceInfo;
	}

	public String getTraceReference() {
        return traceReference;
    }

    public void setTraceReference(String traceReference) {
        this.traceReference = traceReference;
    }

    public String getOriginalPaymentReference() {
        return originalPaymentReference;
    }

    public void setOriginalPaymentReference(String originalPaymentReference) {
        this.originalPaymentReference = originalPaymentReference;
    }

    public String getOriginalPaymentValueDate() {
        return originalPaymentValueDate;
    }

    public void setOriginalPaymentValueDate(String originalPaymentValueDate) {
        this.originalPaymentValueDate = originalPaymentValueDate;
    }

    public String getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(String requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public String getOriginalDebtorBic() {
        return originalDebtorBic;
    }

    public void setOriginalDebtorBic(String originalDebtorBic) {
        this.originalDebtorBic = originalDebtorBic;
    }

    public String getOriginalTransactionReference() {
        return originalTransactionReference;
    }

    public void setOriginalTransactionReference(String originalTransactionReference) {
        this.originalTransactionReference = originalTransactionReference;
    }

	@Override
	public String toString() {
		return "IpsReturnPaymentRequestBo [traceReference=" + traceReference + ", originalPaymentReference="
				+ originalPaymentReference + ", originalPaymentValueDate=" + originalPaymentValueDate
				+ ", requestedAmount=" + requestedAmount + ", originalDebtorBic=" + originalDebtorBic
				+ ", originalTransactionReference=" + originalTransactionReference + ", debtorName=" + debtorName
				+ ", debtorAccount=" + debtorAccount + ", creditorName=" + creditorName + ", creditorAccount="
				+ creditorAccount + ", creditorBic=" + creditorBic + ", purposeCode=" + purposeCode
				+ ", remittanceInfo=" + remittanceInfo + "]";
	}
    
    
}