package com.paylogic.ips.bo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.paylogic.ips.iso20022.bo.BaseDocument;
import com.paylogic.ips.iso20022.bo.pacs002.DocumentPacs002;
import com.paylogic.ips.iso20022.bo.pacs003.DocumentPacs003;
import com.paylogic.ips.iso20022.bo.pacs007.DocumentPacs007;
import com.paylogic.ips.iso20022.bo.pacs008.DocumentPacs008;
import com.paylogic.ips.iso20022.bo.pacs028.DocumentPacs028;
import com.paylogic.ips.iso20022.bo.pain013.DocumentPain013;
import com.paylogic.ips.iso20022.bo.pain014.DocumentPain014;

import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Body", propOrder = { "appHdr", "document" })
@XmlRootElement(name = "Body", namespace = "urn:cma:stp:xsd:stp.1.0")
public class Body {

	@XmlElement(name = "AppHdr", namespace = "urn:iso:std:iso:20022:tech:xsd:head.001.001.02", required = true)
	private AppHdr appHdr;

	@XmlElements({
			@XmlElement(name = "Document", type = DocumentPacs008.class, namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.10") })
	private BaseDocument document;

	public AppHdr getAppHdr() {
		return appHdr;
	}

	public void setAppHdr(AppHdr appHdr) {
		this.appHdr = appHdr;
	}

	public BaseDocument getDocument() {
		return document;
	}

	public void setDocument(BaseDocument document) {
		this.document = document;
	}
}
