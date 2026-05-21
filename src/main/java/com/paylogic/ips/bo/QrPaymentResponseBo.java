package com.paylogic.ips.bo;

public class QrPaymentResponseBo {
	private Header header;
    private Extension extension;
    private String qrExtensionUUID;

    // getters & setters

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Extension getExtension() {
        return extension;
    }

    public void setExtension(Extension extension) {
        this.extension = extension;
    }

    public String getQrExtensionUUID() {
        return qrExtensionUUID;
    }

    public void setQrExtensionUUID(String qrExtensionUUID) {
        this.qrExtensionUUID = qrExtensionUUID;
    }

	@Override
	public String toString() {
		return "QrPaymentResponseBo [header=" + header + ", extension=" + extension + ", qrExtensionUUID="
				+ qrExtensionUUID + "]";
	}


    

    

    

    
}
