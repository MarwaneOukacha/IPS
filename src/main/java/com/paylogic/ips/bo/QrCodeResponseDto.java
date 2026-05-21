package com.paylogic.ips.bo;
public class QrCodeResponseDto {

    private String qrHeaderUUID;
    private String qrAsText;
    private String qrExtensionUUID;

    public String getQrHeaderUUID() {
        return qrHeaderUUID;
    }

    public void setQrHeaderUUID(String qrHeaderUUID) {
        this.qrHeaderUUID = qrHeaderUUID;
    }

    public String getQrAsText() {
        return qrAsText;
    }

    public void setQrAsText(String qrAsText) {
        this.qrAsText = qrAsText;
    }

    public String getQrExtensionUUID() {
        return qrExtensionUUID;
    }

    public void setQrExtensionUUID(String qrExtensionUUID) {
        this.qrExtensionUUID = qrExtensionUUID;
    }
}