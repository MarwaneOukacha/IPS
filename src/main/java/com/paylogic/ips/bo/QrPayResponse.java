package com.paylogic.ips.bo;

public class QrPayResponse {
    private String status;
    private String traceReference;
    private String message;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTraceReference() {
		return traceReference;
	}
	public void setTraceReference(String traceReference) {
		this.traceReference = traceReference;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "QrPayResponse [status=" + status + ", traceReference=" + traceReference + ", message=" + message + "]";
	}
    
    
}