//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2023.10.26 à 04:52:37 PM CAT 
//


package com.paylogic.ips.iso20022.bo.pain014;

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
 *         &lt;element name="CdtrPmtActvtnReqStsRpt" type="{urn:iso:std:iso:20022:tech:xsd:pain.014.001.10}CreditorPaymentActivationRequestStatusReportV10"/&gt;
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
@XmlType(name = "DocumentPain014", propOrder = {
    "cdtrPmtActvtnReqStsRpt"
})
public class DocumentPain014 extends PainBaseDocument{

    @XmlElement(name = "CdtrPmtActvtnReqStsRpt", required = true)
    protected CreditorPaymentActivationRequestStatusReportV10 cdtrPmtActvtnReqStsRpt;

    /**
     * Obtient la valeur de la propriété cdtrPmtActvtnReqStsRpt.
     * 
     * @return
     *     possible object is
     *     {@link CreditorPaymentActivationRequestStatusReportV10 }
     *     
     */
    public CreditorPaymentActivationRequestStatusReportV10 getCdtrPmtActvtnReqStsRpt() {
        return cdtrPmtActvtnReqStsRpt;
    }

    /**
     * Définit la valeur de la propriété cdtrPmtActvtnReqStsRpt.
     * 
     * @param value
     *     allowed object is
     *     {@link CreditorPaymentActivationRequestStatusReportV10 }
     *     
     */
    public void setCdtrPmtActvtnReqStsRpt(CreditorPaymentActivationRequestStatusReportV10 value) {
        this.cdtrPmtActvtnReqStsRpt = value;
    }
    @Override
	protected void initVersion() {
		functionality = "pain";
		version = "014";
		variant = "001.10";		
	}

	@Override
	public String getMsgId() {
		return cdtrPmtActvtnReqStsRpt.grpHdr.msgId;
	}

	@Override
	public XMLGregorianCalendar getCreDtTm() {
		return cdtrPmtActvtnReqStsRpt.grpHdr.creDtTm;
	}
}
