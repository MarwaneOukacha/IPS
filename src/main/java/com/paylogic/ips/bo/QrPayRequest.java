package com.paylogic.ips.bo;

import com.paylogic.ama.core.model.Payment;

public class QrPayRequest {
    private String qrUuid;
    private Payment payment;
	public String getQrUuid() {
		return qrUuid;
	}
	public void setQrUuid(String qrUuid) {
		this.qrUuid = qrUuid;
	}
	public Payment getPayment() {
		return payment;
	}
	public void setPayment(Payment payment) {
		this.payment = payment;
	}
	@Override
	public String toString() {
		return "QrPayRequest [qrUuid=" + qrUuid + ", payment=" + payment + "]";
	}
    
    
    
    
}