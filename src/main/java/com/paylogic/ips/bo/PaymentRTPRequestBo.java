package com.paylogic.ips.bo;

public class PaymentRTPRequestBo {
	private String paymentService;
    private PaymentAmountBo paymentAmount;

    @Override
	public String toString() {
		return "PaymentRTPRequestBo [paymentService=" + paymentService + ", paymentAmount=" + paymentAmount + "]";
	}

	public String getPaymentService() {
        return paymentService;
    }

    public void setPaymentService(String paymentService) {
        this.paymentService = paymentService;
    }

    public PaymentAmountBo getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(PaymentAmountBo paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    

}
