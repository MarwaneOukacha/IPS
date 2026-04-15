//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2023.10.26 à 04:52:33 PM CAT 
//


package com.paylogic.ips.iso20022.bo.pacs008;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour BranchAndFinancialInstitutionIdentification6 complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="BranchAndFinancialInstitutionIdentification6"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="FinInstnId" type="{urn:iso:std:iso:20022:tech:xsd:pacs.008.001.11}FinancialInstitutionIdentification18"/&gt;
 *         &lt;element name="BrnchId" type="{urn:iso:std:iso:20022:tech:xsd:pacs.008.001.11}BranchData3" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BranchAndFinancialInstitutionIdentification6", propOrder = {
    "finInstnId",
    "brnchId"
})
public class BranchAndFinancialInstitutionIdentification6 {

    @XmlElement(name = "FinInstnId", required = true)
    protected FinancialInstitutionIdentification18 finInstnId;
    @XmlElement(name = "BrnchId")
    protected BranchData3 brnchId;

    /**
     * Obtient la valeur de la propriété finInstnId.
     * 
     * @return
     *     possible object is
     *     {@link FinancialInstitutionIdentification18 }
     *     
     */
    public FinancialInstitutionIdentification18 getFinInstnId() {
        return finInstnId;
    }

    /**
     * Définit la valeur de la propriété finInstnId.
     * 
     * @param value
     *     allowed object is
     *     {@link FinancialInstitutionIdentification18 }
     *     
     */
    public void setFinInstnId(FinancialInstitutionIdentification18 value) {
        this.finInstnId = value;
    }

    /**
     * Obtient la valeur de la propriété brnchId.
     * 
     * @return
     *     possible object is
     *     {@link BranchData3 }
     *     
     */
    public BranchData3 getBrnchId() {
        return brnchId;
    }

    /**
     * Définit la valeur de la propriété brnchId.
     * 
     * @param value
     *     allowed object is
     *     {@link BranchData3 }
     *     
     */
    public void setBrnchId(BranchData3 value) {
        this.brnchId = value;
    }

	@Override
	public String toString() {
		return "BranchAndFinancialInstitutionIdentification6 {'finInstnId':'" + finInstnId + "', 'brnchId':'" + brnchId
				+ "'}";
	}

}
