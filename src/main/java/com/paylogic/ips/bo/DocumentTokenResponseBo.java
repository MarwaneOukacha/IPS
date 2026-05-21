package com.paylogic.ips.bo;

public class DocumentTokenResponseBo {

    private String documentToken;
    private Integer expires_in;
    private Integer lock_ttl;

    public String getDocumentToken() {
        return documentToken;
    }

    public void setDocumentToken(String documentToken) {
        this.documentToken = documentToken;
    }

    public Integer getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Integer expires_in) {
        this.expires_in = expires_in;
    }

    public Integer getLock_ttl() {
        return lock_ttl;
    }

    public void setLock_ttl(Integer lock_ttl) {
        this.lock_ttl = lock_ttl;
    }

    @Override
    public String toString() {
        return "DocumentTokenResponseBo{" +
                "documentToken='" + documentToken + '\'' +
                ", expires_in=" + expires_in +
                ", lock_ttl=" + lock_ttl +
                '}';
    }
}