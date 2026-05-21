package com.paylogic.ips.bo;

import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)

public class IpsRTPPaymentRequestBo {

    private String traceReference;
    private String type;
    private String sender;
    private String receiver;
    private String documentToken;
    private String document;

    public String getTraceReference() {
        return traceReference;
    }

    public void setTraceReference(String traceReference) {
        this.traceReference = traceReference;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getDocumentToken() {
        return documentToken;
    }

    public void setDocumentToken(String documentToken) {
        this.documentToken = documentToken;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    @Override
    public String toString() {
        return "IpsRTPPaymentRequestBo{" +
                "traceReference='" + traceReference + '\'' +
                ", type='" + type + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", documentToken='" + documentToken + '\'' +
                ", document='" + document + '\'' +
                '}';
    }
}