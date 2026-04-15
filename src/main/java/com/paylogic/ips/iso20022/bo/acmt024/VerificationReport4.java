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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour VerificationReport4 complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="VerificationReport4"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="OrgnlId" type="{urn:iso:std:iso:20022:tech:xsd:acmt.024.001.03}Max35Text"/&gt;
 *         &lt;element name="Vrfctn" type="{urn:iso:std:iso:20022:tech:xsd:acmt.024.001.03}IdentificationVerificationIndicator"/&gt;
 *         &lt;element name="Rsn" type="{urn:iso:std:iso:20022:tech:xsd:acmt.024.001.03}VerificationReason1Choice" minOccurs="0"/&gt;
 *         &lt;element name="OrgnlPtyAndAcctId" type="{urn:iso:std:iso:20022:tech:xsd:acmt.024.001.03}IdentificationInformation4" minOccurs="0"/&gt;
 *         &lt;element name="UpdtdPtyAndAcctId" type="{urn:iso:std:iso:20022:tech:xsd:acmt.024.001.03}IdentificationInformation4" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VerificationReport4", propOrder = {
    "orgnlId",
    "vrfctn",
    "rsn",
    "orgnlPtyAndAcctId",
    "updtdPtyAndAcctId"
})
public class VerificationReport4 {

    @XmlElement(name = "OrgnlId", required = true)
    protected String orgnlId;
    @XmlElement(name = "Vrfctn")
    protected boolean vrfctn;
    @XmlElement(name = "Rsn")
    protected VerificationReason1Choice rsn;
    @XmlElement(name = "OrgnlPtyAndAcctId")
    protected IdentificationInformation4 orgnlPtyAndAcctId;
    @XmlElement(name = "UpdtdPtyAndAcctId")
    protected IdentificationInformation4 updtdPtyAndAcctId;

    /**
     * Obtient la valeur de la propriété orgnlId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrgnlId() {
        return orgnlId;
    }

    /**
     * Définit la valeur de la propriété orgnlId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrgnlId(String value) {
        this.orgnlId = value;
    }

    /**
     * Obtient la valeur de la propriété vrfctn.
     * 
     */
    public boolean isVrfctn() {
        return vrfctn;
    }

    /**
     * Définit la valeur de la propriété vrfctn.
     * 
     */
    public void setVrfctn(boolean value) {
        this.vrfctn = value;
    }

    /**
     * Obtient la valeur de la propriété rsn.
     * 
     * @return
     *     possible object is
     *     {@link VerificationReason1Choice }
     *     
     */
    public VerificationReason1Choice getRsn() {
        return rsn;
    }

    /**
     * Définit la valeur de la propriété rsn.
     * 
     * @param value
     *     allowed object is
     *     {@link VerificationReason1Choice }
     *     
     */
    public void setRsn(VerificationReason1Choice value) {
        this.rsn = value;
    }

    /**
     * Obtient la valeur de la propriété orgnlPtyAndAcctId.
     * 
     * @return
     *     possible object is
     *     {@link IdentificationInformation4 }
     *     
     */
    public IdentificationInformation4 getOrgnlPtyAndAcctId() {
        return orgnlPtyAndAcctId;
    }

    /**
     * Définit la valeur de la propriété orgnlPtyAndAcctId.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentificationInformation4 }
     *     
     */
    public void setOrgnlPtyAndAcctId(IdentificationInformation4 value) {
        this.orgnlPtyAndAcctId = value;
    }

    /**
     * Obtient la valeur de la propriété updtdPtyAndAcctId.
     * 
     * @return
     *     possible object is
     *     {@link IdentificationInformation4 }
     *     
     */
    public IdentificationInformation4 getUpdtdPtyAndAcctId() {
        return updtdPtyAndAcctId;
    }

    /**
     * Définit la valeur de la propriété updtdPtyAndAcctId.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentificationInformation4 }
     *     
     */
    public void setUpdtdPtyAndAcctId(IdentificationInformation4 value) {
        this.updtdPtyAndAcctId = value;
    }

	@Override
	public String toString() {
		return "VerificationReport4 {'orgnlId':'" + orgnlId + "', 'vrfctn':'" + vrfctn + "', 'rsn':'" + rsn
				+ "', 'orgnlPtyAndAcctId':'" + orgnlPtyAndAcctId + "', 'updtdPtyAndAcctId':'" + updtdPtyAndAcctId
				+ "'}";
	}

}
