//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2023.10.26 à 04:52:34 PM CAT 
//


package com.paylogic.ips.iso20022.bo.acmt23;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour IdentificationVerification4 complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="IdentificationVerification4"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Id" type="{urn:iso:std:iso:20022:tech:xsd:acmt.023.001.03}Max35Text"/&gt;
 *         &lt;element name="PtyAndAcctId" type="{urn:iso:std:iso:20022:tech:xsd:acmt.023.001.03}IdentificationInformation4"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdentificationVerification4", propOrder = {
    "id",
    "ptyAndAcctId"
})
public class IdentificationVerification4 {

    @XmlElement(name = "Id", required = true)
    protected String id;
    @XmlElement(name = "PtyAndAcctId", required = true)
    protected IdentificationInformation4 ptyAndAcctId;

    /**
     * Obtient la valeur de la propriété id.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Définit la valeur de la propriété id.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Obtient la valeur de la propriété ptyAndAcctId.
     * 
     * @return
     *     possible object is
     *     {@link IdentificationInformation4 }
     *     
     */
    public IdentificationInformation4 getPtyAndAcctId() {
        return ptyAndAcctId;
    }

    /**
     * Définit la valeur de la propriété ptyAndAcctId.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentificationInformation4 }
     *     
     */
    public void setPtyAndAcctId(IdentificationInformation4 value) {
        this.ptyAndAcctId = value;
    }

	@Override
	public String toString() {
		return "IdentificationVerification4 {'id':'" + id + "', 'ptyAndAcctId':'" + ptyAndAcctId + "'}";
	}

}
