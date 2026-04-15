package com.paylogic.ips.bo;



public class QrResolveRequest {
    private String qrData; // raw QR string OR extracted UUID

	public String getQrData() {
		return qrData;
	}

	public void setQrData(String qrData) {
		this.qrData = qrData;
	}
    
    
}
