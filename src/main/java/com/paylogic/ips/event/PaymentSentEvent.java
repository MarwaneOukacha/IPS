package com.paylogic.ips.event;

public class PaymentSentEvent {

    private String traceReference;

    public PaymentSentEvent(String traceReference) {
        this.traceReference = traceReference;
    }

    public String getTraceReference() {
        return traceReference;
    }
}