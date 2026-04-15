//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2023.10.26 à 04:52:36 PM CAT 
//


package com.paylogic.ips.iso20022.bo.pain013;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour PaymentInstruction42 complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="PaymentInstruction42"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="PmtInfId" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}Max35Text" minOccurs="0"/&gt;
 *         &lt;element name="PmtMtd" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}PaymentMethod7Code"/&gt;
 *         &lt;element name="ReqdAdvcTp" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}AdviceType1" minOccurs="0"/&gt;
 *         &lt;element name="PmtTpInf" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}PaymentTypeInformation26" minOccurs="0"/&gt;
 *         &lt;element name="ReqdExctnDt" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}DateAndDateTime2Choice" minOccurs="0"/&gt;
 *         &lt;element name="XpryDt" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}DateAndDateTime2Choice" minOccurs="0"/&gt;
 *         &lt;element name="PmtCond" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}PaymentCondition1" minOccurs="0"/&gt;
 *         &lt;element name="Dbtr" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}PartyIdentification135"/&gt;
 *         &lt;element name="DbtrAcct" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}CashAccount40" minOccurs="0"/&gt;
 *         &lt;element name="DbtrAgt" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}BranchAndFinancialInstitutionIdentification6"/&gt;
 *         &lt;element name="DbtrAgtAcct" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}CashAccount40" minOccurs="0"/&gt;
 *         &lt;element name="UltmtDbtr" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}PartyIdentification135" minOccurs="0"/&gt;
 *         &lt;element name="ChrgBr" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}ChargeBearerType1Code" minOccurs="0"/&gt;
 *         &lt;element name="CdtTrfTx" type="{urn:iso:std:iso:20022:tech:xsd:pain.013.001.10}CreditTransferTransaction57" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PaymentInstruction42", propOrder = {
    "pmtInfId",
    "pmtMtd",
    "reqdAdvcTp",
    "pmtTpInf",
    "reqdExctnDt",
    "xpryDt",
    "pmtCond",
    "dbtr",
    "dbtrAcct",
    "dbtrAgt",
    "dbtrAgtAcct",
    "ultmtDbtr",
    "chrgBr",
    "cdtTrfTx"
})
public class PaymentInstruction42 {

    @XmlElement(name = "PmtInfId")
    protected String pmtInfId;
    @XmlElement(name = "PmtMtd", required = true)
    @XmlSchemaType(name = "string")
    protected PaymentMethod7Code pmtMtd;
    @XmlElement(name = "ReqdAdvcTp")
    protected AdviceType1 reqdAdvcTp;
    @XmlElement(name = "PmtTpInf")
    protected PaymentTypeInformation26 pmtTpInf;
    @XmlElement(name = "ReqdExctnDt")
    protected DateAndDateTime2Choice reqdExctnDt;
    @XmlElement(name = "XpryDt")
    protected DateAndDateTime2Choice xpryDt;
    @XmlElement(name = "PmtCond")
    protected PaymentCondition1 pmtCond;
    @XmlElement(name = "Dbtr", required = true)
    protected PartyIdentification135 dbtr;
    @XmlElement(name = "DbtrAcct")
    protected CashAccount40 dbtrAcct;
    @XmlElement(name = "DbtrAgt", required = true)
    protected BranchAndFinancialInstitutionIdentification6 dbtrAgt;
    @XmlElement(name = "DbtrAgtAcct")
    protected CashAccount40 dbtrAgtAcct;
    @XmlElement(name = "UltmtDbtr")
    protected PartyIdentification135 ultmtDbtr;
    @XmlElement(name = "ChrgBr")
    @XmlSchemaType(name = "string")
    protected ChargeBearerType1Code chrgBr;
    @XmlElement(name = "CdtTrfTx", required = true)
    protected List<CreditTransferTransaction57> cdtTrfTx;

    /**
     * Obtient la valeur de la propriété pmtInfId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPmtInfId() {
        return pmtInfId;
    }

    /**
     * Définit la valeur de la propriété pmtInfId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPmtInfId(String value) {
        this.pmtInfId = value;
    }

    /**
     * Obtient la valeur de la propriété pmtMtd.
     * 
     * @return
     *     possible object is
     *     {@link PaymentMethod7Code }
     *     
     */
    public PaymentMethod7Code getPmtMtd() {
        return pmtMtd;
    }

    /**
     * Définit la valeur de la propriété pmtMtd.
     * 
     * @param value
     *     allowed object is
     *     {@link PaymentMethod7Code }
     *     
     */
    public void setPmtMtd(PaymentMethod7Code value) {
        this.pmtMtd = value;
    }

    /**
     * Obtient la valeur de la propriété reqdAdvcTp.
     * 
     * @return
     *     possible object is
     *     {@link AdviceType1 }
     *     
     */
    public AdviceType1 getReqdAdvcTp() {
        return reqdAdvcTp;
    }

    /**
     * Définit la valeur de la propriété reqdAdvcTp.
     * 
     * @param value
     *     allowed object is
     *     {@link AdviceType1 }
     *     
     */
    public void setReqdAdvcTp(AdviceType1 value) {
        this.reqdAdvcTp = value;
    }

    /**
     * Obtient la valeur de la propriété pmtTpInf.
     * 
     * @return
     *     possible object is
     *     {@link PaymentTypeInformation26 }
     *     
     */
    public PaymentTypeInformation26 getPmtTpInf() {
        return pmtTpInf;
    }

    /**
     * Définit la valeur de la propriété pmtTpInf.
     * 
     * @param value
     *     allowed object is
     *     {@link PaymentTypeInformation26 }
     *     
     */
    public void setPmtTpInf(PaymentTypeInformation26 value) {
        this.pmtTpInf = value;
    }

    /**
     * Obtient la valeur de la propriété reqdExctnDt.
     * 
     * @return
     *     possible object is
     *     {@link DateAndDateTime2Choice }
     *     
     */
    public DateAndDateTime2Choice getReqdExctnDt() {
        return reqdExctnDt;
    }

    /**
     * Définit la valeur de la propriété reqdExctnDt.
     * 
     * @param value
     *     allowed object is
     *     {@link DateAndDateTime2Choice }
     *     
     */
    public void setReqdExctnDt(DateAndDateTime2Choice value) {
        this.reqdExctnDt = value;
    }

    /**
     * Obtient la valeur de la propriété xpryDt.
     * 
     * @return
     *     possible object is
     *     {@link DateAndDateTime2Choice }
     *     
     */
    public DateAndDateTime2Choice getXpryDt() {
        return xpryDt;
    }

    /**
     * Définit la valeur de la propriété xpryDt.
     * 
     * @param value
     *     allowed object is
     *     {@link DateAndDateTime2Choice }
     *     
     */
    public void setXpryDt(DateAndDateTime2Choice value) {
        this.xpryDt = value;
    }

    /**
     * Obtient la valeur de la propriété pmtCond.
     * 
     * @return
     *     possible object is
     *     {@link PaymentCondition1 }
     *     
     */
    public PaymentCondition1 getPmtCond() {
        return pmtCond;
    }

    /**
     * Définit la valeur de la propriété pmtCond.
     * 
     * @param value
     *     allowed object is
     *     {@link PaymentCondition1 }
     *     
     */
    public void setPmtCond(PaymentCondition1 value) {
        this.pmtCond = value;
    }

    /**
     * Obtient la valeur de la propriété dbtr.
     * 
     * @return
     *     possible object is
     *     {@link PartyIdentification135 }
     *     
     */
    public PartyIdentification135 getDbtr() {
        return dbtr;
    }

    /**
     * Définit la valeur de la propriété dbtr.
     * 
     * @param value
     *     allowed object is
     *     {@link PartyIdentification135 }
     *     
     */
    public void setDbtr(PartyIdentification135 value) {
        this.dbtr = value;
    }

    /**
     * Obtient la valeur de la propriété dbtrAcct.
     * 
     * @return
     *     possible object is
     *     {@link CashAccount40 }
     *     
     */
    public CashAccount40 getDbtrAcct() {
        return dbtrAcct;
    }

    /**
     * Définit la valeur de la propriété dbtrAcct.
     * 
     * @param value
     *     allowed object is
     *     {@link CashAccount40 }
     *     
     */
    public void setDbtrAcct(CashAccount40 value) {
        this.dbtrAcct = value;
    }

    /**
     * Obtient la valeur de la propriété dbtrAgt.
     * 
     * @return
     *     possible object is
     *     {@link BranchAndFinancialInstitutionIdentification6 }
     *     
     */
    public BranchAndFinancialInstitutionIdentification6 getDbtrAgt() {
        return dbtrAgt;
    }

    /**
     * Définit la valeur de la propriété dbtrAgt.
     * 
     * @param value
     *     allowed object is
     *     {@link BranchAndFinancialInstitutionIdentification6 }
     *     
     */
    public void setDbtrAgt(BranchAndFinancialInstitutionIdentification6 value) {
        this.dbtrAgt = value;
    }

    /**
     * Obtient la valeur de la propriété dbtrAgtAcct.
     * 
     * @return
     *     possible object is
     *     {@link CashAccount40 }
     *     
     */
    public CashAccount40 getDbtrAgtAcct() {
        return dbtrAgtAcct;
    }

    /**
     * Définit la valeur de la propriété dbtrAgtAcct.
     * 
     * @param value
     *     allowed object is
     *     {@link CashAccount40 }
     *     
     */
    public void setDbtrAgtAcct(CashAccount40 value) {
        this.dbtrAgtAcct = value;
    }

    /**
     * Obtient la valeur de la propriété ultmtDbtr.
     * 
     * @return
     *     possible object is
     *     {@link PartyIdentification135 }
     *     
     */
    public PartyIdentification135 getUltmtDbtr() {
        return ultmtDbtr;
    }

    /**
     * Définit la valeur de la propriété ultmtDbtr.
     * 
     * @param value
     *     allowed object is
     *     {@link PartyIdentification135 }
     *     
     */
    public void setUltmtDbtr(PartyIdentification135 value) {
        this.ultmtDbtr = value;
    }

    /**
     * Obtient la valeur de la propriété chrgBr.
     * 
     * @return
     *     possible object is
     *     {@link ChargeBearerType1Code }
     *     
     */
    public ChargeBearerType1Code getChrgBr() {
        return chrgBr;
    }

    /**
     * Définit la valeur de la propriété chrgBr.
     * 
     * @param value
     *     allowed object is
     *     {@link ChargeBearerType1Code }
     *     
     */
    public void setChrgBr(ChargeBearerType1Code value) {
        this.chrgBr = value;
    }

    /**
     * Gets the value of the cdtTrfTx property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cdtTrfTx property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCdtTrfTx().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CreditTransferTransaction57 }
     * 
     * 
     */
    public List<CreditTransferTransaction57> getCdtTrfTx() {
        if (cdtTrfTx == null) {
            cdtTrfTx = new ArrayList<CreditTransferTransaction57>();
        }
        return this.cdtTrfTx;
    }

}
