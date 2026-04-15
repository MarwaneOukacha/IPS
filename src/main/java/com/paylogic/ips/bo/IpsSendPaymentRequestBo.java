package com.paylogic.ips.bo;

public class IpsSendPaymentRequestBo {
	private String traceReference;
	private String type;
	private String sender;
	private String receiver;
	private String dataPdu;
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
	public String getDocument() {
		return dataPdu;
	}
	public void setDocument(String document) {
		this.dataPdu = document;
	}
	@Override
	public String toString() {
		return "IpsSendPaymentRequestBo [traceReference=" + traceReference + ", type=" + type + ", sender=" + sender
				+ ", receiver=" + receiver + ", document=" + dataPdu + "]";
	}
	

}
