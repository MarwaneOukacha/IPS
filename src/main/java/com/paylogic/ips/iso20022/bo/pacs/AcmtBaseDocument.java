package com.paylogic.ips.iso20022.bo.pacs;

import javax.xml.bind.annotation.XmlTransient;

import com.paylogic.ips.iso20022.bo.BaseDocument;


@XmlTransient
public abstract class AcmtBaseDocument extends BaseDocument{
	public abstract String getAssgneId();
	public abstract String getAssgnrId();
	public abstract String getVerificationId();
	public abstract String getAccountId();
	public AcmtBaseDocument() {
		super();
	}
	
}
