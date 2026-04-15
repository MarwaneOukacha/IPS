//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2023.10.26 à 04:52:36 PM CAT 
//


package com.paylogic.ips.iso20022.bo.pain013;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import com.paylogic.ips.iso20022.bo.PainBaseDocument;



/**
 * <p>Classe Java pour Document complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="Document"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="CdtrPmtActvtnReq" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}CreditorPaymentActivationRequestV10"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlRootElement(name="Document")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DocumentPain013", propOrder = {
    "cdtrPmtActvtnReq"
})
public class DocumentPain013 extends PainBaseDocument{

    @XmlElement(name = "CdtrPmtActvtnReq", required = true)
    protected CreditorPaymentActivationRequestV10 cdtrPmtActvtnReq;

    /**
     * Obtient la valeur de la propriété cdtrPmtActvtnReq.
     * 
     * @return
     *     possible object is
     *     {@link CreditorPaymentActivationRequestV10 }
     *     
     */
    public CreditorPaymentActivationRequestV10 getCdtrPmtActvtnReq() {
        return cdtrPmtActvtnReq;
    }

    /**
     * Définit la valeur de la propriété cdtrPmtActvtnReq.
     * 
     * @param value
     *     allowed object is
     *     {@link CreditorPaymentActivationRequestV10 }
     *     
     */
    public void setCdtrPmtActvtnReq(CreditorPaymentActivationRequestV10 value) {
        this.cdtrPmtActvtnReq = value;
    }

	@Override
	protected void initVersion() {
		functionality = "pain";
		version = "013";
		variant = "001.10";		
	}

	@Override
	public String getMsgId() {
		return cdtrPmtActvtnReq.grpHdr.msgId;
	}

	@Override
	public XMLGregorianCalendar getCreDtTm() {
		return cdtrPmtActvtnReq.grpHdr.creDtTm;
	}

}
