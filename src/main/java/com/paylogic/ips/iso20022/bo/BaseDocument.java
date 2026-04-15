package com.paylogic.ips.iso20022.bo;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlTransient
public abstract class BaseDocument {
	@XmlTransient
	protected String functionality;
	@XmlTransient
	protected String variant;
	@XmlTransient
	protected String version;
	protected abstract void initVersion();
	public abstract String getMsgId();
	public abstract XMLGregorianCalendar getCreDtTm();

	public BaseDocument() {
		initVersion();
	}
	
	public String getDocumentVersion() {
		return functionality+"."+variant+"."+version;
	}
}
