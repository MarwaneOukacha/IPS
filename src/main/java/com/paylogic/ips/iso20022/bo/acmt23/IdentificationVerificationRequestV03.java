//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2023.10.26 à 04:52:34 PM CAT 
//


package com.paylogic.ips.iso20022.bo.acmt23;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour IdentificationVerificationRequestV03 complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="IdentificationVerificationRequestV03"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Assgnmt" type="{urn:iso:std:iso:20022:tech:xsd:acmt.023.001.03}IdentificationAssignment3"/&gt;
 *         &lt;element name="Vrfctn" type="{urn:iso:std:iso:20022:tech:xsd:acmt.023.001.03}IdentificationVerification4" maxOccurs="unbounded"/&gt;
 *         &lt;element name="SplmtryData" type="{urn:iso:std:iso:20022:tech:xsd:acmt.023.001.03}SupplementaryData1" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdentificationVerificationRequestV03", propOrder = {
    "assgnmt",
    "vrfctn",
    "splmtryData"
})
public class IdentificationVerificationRequestV03 {

    @XmlElement(name = "Assgnmt", required = true)
    protected IdentificationAssignment3 assgnmt;
    @XmlElement(name = "Vrfctn", required = true)
    protected List<IdentificationVerification4> vrfctn;
    @XmlElement(name = "SplmtryData")
    protected List<SupplementaryData1> splmtryData;

    /**
     * Obtient la valeur de la propriété assgnmt.
     * 
     * @return
     *     possible object is
     *     {@link IdentificationAssignment3 }
     *     
     */
    public IdentificationAssignment3 getAssgnmt() {
        return assgnmt;
    }

    /**
     * Définit la valeur de la propriété assgnmt.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentificationAssignment3 }
     *     
     */
    public void setAssgnmt(IdentificationAssignment3 value) {
        this.assgnmt = value;
    }

    /**
     * Gets the value of the vrfctn property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the vrfctn property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVrfctn().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IdentificationVerification4 }
     * 
     * 
     */
    public List<IdentificationVerification4> getVrfctn() {
        if (vrfctn == null) {
            vrfctn = new ArrayList<IdentificationVerification4>();
        }
        return this.vrfctn;
    }

    /**
     * Gets the value of the splmtryData property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the splmtryData property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSplmtryData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SupplementaryData1 }
     * 
     * 
     */
    public List<SupplementaryData1> getSplmtryData() {
        if (splmtryData == null) {
            splmtryData = new ArrayList<SupplementaryData1>();
        }
        return this.splmtryData;
    }

	@Override
	public String toString() {
		return "IdentificationVerificationRequestV03 {'assgnmt':'" + assgnmt + "', 'vrfctn':'" + vrfctn
				+ "', 'splmtryData':'" + splmtryData + "'}";
	}

}
