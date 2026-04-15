package com.paylogic.ips.bo;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB class for AppHdr (ISO 20022 head.001.001.02)
 */
@XmlRootElement(name = "AppHdr", namespace = "urn:iso:std:iso:20022:tech:xsd:head.001.001.02")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"fr","to","bizMsgIdr","msgDefIdr","bizSvc","creDt","prty"})
public class AppHdr {

    @XmlElement(name = "Fr", required = true)
    protected Party fr;

    @XmlElement(name = "To", required = true)
    protected Party to;

    @XmlElement(name = "BizMsgIdr", required = true)
    protected String bizMsgIdr;

    @XmlElement(name = "MsgDefIdr", required = true)
    protected String msgDefIdr;

    @XmlElement(name = "BizSvc", required = true)
    protected String bizSvc;

    @XmlElement(name = "CreDt", required = true)
    protected String creDt; // ISODateTime string

    @XmlElement(name = "Prty", required = true)
    protected String prty;

    // Getters and Setters
    public Party getFr() { return fr; }
    public void setFr(Party fr) { this.fr = fr; }

    public Party getTo() { return to; }
    public void setTo(Party to) { this.to = to; }

    public String getBizMsgIdr() { return bizMsgIdr; }
    public void setBizMsgIdr(String bizMsgIdr) { this.bizMsgIdr = bizMsgIdr; }

    public String getMsgDefIdr() { return msgDefIdr; }
    public void setMsgDefIdr(String msgDefIdr) { this.msgDefIdr = msgDefIdr; }

    public String getBizSvc() { return bizSvc; }
    public void setBizSvc(String bizSvc) { this.bizSvc = bizSvc; }

    public String getCreDt() { return creDt; }
    public void setCreDt(String creDt) { this.creDt = creDt; }

    public String getPrty() { return prty; }
    public void setPrty(String prty) { this.prty = prty; }

    // Nested classes for Party -> FIId -> FinInstnId -> BICFI
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"fiId"})
    public static class Party {
        @XmlElement(name = "FIId", required = true)
        protected FIId fiId;

        public FIId getFiId() { return fiId; }
        public void setFiId(FIId fiId) { this.fiId = fiId; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"finInstnId"})
    public static class FIId {
        @XmlElement(name = "FinInstnId", required = true)
        protected FinInstnId finInstnId;

        public FinInstnId getFinInstnId() { return finInstnId; }
        public void setFinInstnId(FinInstnId finInstnId) { this.finInstnId = finInstnId; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"bicfi"})
    public static class FinInstnId {
        @XmlElement(name = "BICFI", required = true)
        protected String bicfi;

        public String getBicfi() { return bicfi; }
        public void setBicfi(String bicfi) { this.bicfi = bicfi; }
    }

	@Override
	public String toString() {
		return "AppHdr [fr=" + fr + ", to=" + to + ", bizMsgIdr=" + bizMsgIdr + ", msgDefIdr=" + msgDefIdr + ", bizSvc="
				+ bizSvc + ", creDt=" + creDt + ", prty=" + prty + "]";
	}
    
    
}
