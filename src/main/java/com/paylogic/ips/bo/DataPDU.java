
package com.paylogic.ips.bo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "DataPDU", namespace = "urn:cma:stp:xsd:stp.1.0")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataPDU {
    @XmlElement(name = "Body", namespace = "urn:cma:stp:xsd:stp.1.0")
    private Body body;

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "DataPDU [body=" + body + "]";
	}
    
    

    // getters & setters
}
