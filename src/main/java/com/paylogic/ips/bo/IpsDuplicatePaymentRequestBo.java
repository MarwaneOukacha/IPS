package com.paylogic.ips.bo;
public class IpsDuplicatePaymentRequestBo {

    private String traceReference;
    private String originalPaymentReference;
    private String originalPaymentValueDate;
    private String originalDebitorBic;

    
    
    public String getOriginalDebitorBic() {
		return originalDebitorBic;
	}

	public void setOriginalDebitorBic(String originalDebitorBic) {
		this.originalDebitorBic = originalDebitorBic;
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

	@Override
	public String toString() {
		return "IpsDuplicatePaymentRequestBo [traceReference=" + traceReference + ", originalPaymentReference="
				+ originalPaymentReference + ", originalPaymentValueDate=" + originalPaymentValueDate
				+ ", originalDebitorBic=" + originalDebitorBic + "]";
	}

	

    
}