//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2023.10.26 à 04:52:35 PM CAT 
//


package com.paylogic.ips.iso20022.bo.acmt024;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import com.paylogic.ips.iso20022.bo.pacs.AcmtBaseDocument;


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
 *         &lt;element name="IdVrfctnRpt" type="{urn:iso:std:iso:20022:tech:xsd:acmt.024.001.03}IdentificationVerificationReportV03"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlRootElement(name = "Document")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DocumentAcmt024", propOrder = {
    "idVrfctnRpt"
})
public class DocumentAcmt024 extends AcmtBaseDocument{

    @XmlElement(name = "IdVrfctnRpt", required = true)
    protected IdentificationVerificationReportV03 idVrfctnRpt;

    /**
     * Obtient la valeur de la propriété idVrfctnRpt.
     * 
     * @return
     *     possible object is
     *     {@link IdentificationVerificationReportV03 }
     *     
     */
    public IdentificationVerificationReportV03 getIdVrfctnRpt() {
        return idVrfctnRpt;
    }

    /**
     * Définit la valeur de la propriété idVrfctnRpt.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentificationVerificationReportV03 }
     *     
     */
    public void setIdVrfctnRpt(IdentificationVerificationReportV03 value) {
        this.idVrfctnRpt = value;
    }
    
    @Override
	protected void initVersion() {
		functionality = "acmt";
		variant = "024";
		version = "001.03";
		
	}

	@Override
	public String getMsgId() {
		return idVrfctnRpt.assgnmt.msgId;
	}

	@Override
	public XMLGregorianCalendar getCreDtTm() {
		return idVrfctnRpt.assgnmt.creDtTm;
	}

	@Override
	public String getAssgneId() {
		if(idVrfctnRpt.assgnmt != null && idVrfctnRpt.assgnmt.assgne != null && idVrfctnRpt.assgnmt.assgne.agt != null && idVrfctnRpt.assgnmt.assgne.agt.finInstnId != null && idVrfctnRpt.assgnmt.assgne.agt.finInstnId.othr != null) {
			return idVrfctnRpt.assgnmt.assgne.agt.finInstnId.othr.id;
		}else {
			return null;
		}
	}
	
	@Override
	public String getAssgnrId() {
		if(idVrfctnRpt.assgnmt != null && idVrfctnRpt.assgnmt.assgnr != null && idVrfctnRpt.assgnmt.assgnr.agt != null && idVrfctnRpt.assgnmt.assgnr.agt.finInstnId != null && idVrfctnRpt.assgnmt.assgnr.agt.finInstnId.othr != null) {
			return idVrfctnRpt.assgnmt.assgnr.agt.finInstnId.othr.id;
		}else {
			return null;
		}
	}
	
	@Override
	public String getVerificationId() {
		if(idVrfctnRpt.rpt != null && idVrfctnRpt.rpt.size() >= 1) {
			return idVrfctnRpt.rpt.get(0).orgnlId;
		}else {
			return null;
		}
	}

	@Override
	public String getAccountId() {
		if(idVrfctnRpt.rpt != null && idVrfctnRpt.rpt.size() >= 1 && idVrfctnRpt.rpt.get(0).updtdPtyAndAcctId != null &&idVrfctnRpt.rpt.get(0).updtdPtyAndAcctId.acct != null &&idVrfctnRpt.rpt.get(0).updtdPtyAndAcctId.acct.id != null &&idVrfctnRpt.rpt.get(0).updtdPtyAndAcctId.acct.id.othr != null) {
			return idVrfctnRpt.rpt.get(0).updtdPtyAndAcctId.acct.id.othr.id;
		}else {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "DocumentAcmt024 {'idVrfctnRpt':'" + idVrfctnRpt + "'}";
	}
}
