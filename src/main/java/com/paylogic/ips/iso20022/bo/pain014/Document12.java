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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour Document12 complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="Document12"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Tp" type="{urn:iso:std:iso:20022:tech:xsd:pain.014.001.10}DocumentType1Choice"/&gt;
 *         &lt;element name="Id" type="{urn:iso:std:iso:20022:tech:xsd:pain.014.001.10}Max35Text"/&gt;
 *         &lt;element name="IsseDt" type="{urn:iso:std:iso:20022:tech:xsd:pain.014.001.10}DateAndDateTime2Choice"/&gt;
 *         &lt;element name="Nm" type="{urn:iso:std:iso:20022:tech:xsd:pain.014.001.10}Max140Text" minOccurs="0"/&gt;
 *         &lt;element name="LangCd" type="{urn:iso:std:iso:20022:tech:xsd:pain.014.001.10}LanguageCode" minOccurs="0"/&gt;
 *         &lt;element name="Frmt" type="{urn:iso:std:iso:20022:tech:xsd:pain.014.001.10}DocumentFormat1Choice"/&gt;
 *         &lt;element name="FileNm" type="{urn:iso:std:iso:20022:tech:xsd:pain.014.001.10}Max140Text" minOccurs="0"/&gt;
 *         &lt;element name="DgtlSgntr" type="{urn:iso:std:iso:20022:tech:xsd:pain.014.001.10}PartyAndSignature3" minOccurs="0"/&gt;
 *         &lt;element name="Nclsr" type="{urn:iso:std:iso:20022:tech:xsd:pain.014.001.10}Max10MbBinary"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Document12", propOrder = {
    "tp",
    "id",
    "isseDt",
    "nm",
    "langCd",
    "frmt",
    "fileNm",
    "dgtlSgntr",
    "nclsr"
})
public class Document12 {

    @XmlElement(name = "Tp", required = true)
    protected DocumentType1Choice tp;
    @XmlElement(name = "Id", required = true)
    protected String id;
    @XmlElement(name = "IsseDt", required = true)
    protected DateAndDateTime2Choice isseDt;
    @XmlElement(name = "Nm")
    protected String nm;
    @XmlElement(name = "LangCd")
    protected String langCd;
    @XmlElement(name = "Frmt", required = true)
    protected DocumentFormat1Choice frmt;
    @XmlElement(name = "FileNm")
    protected String fileNm;
    @XmlElement(name = "DgtlSgntr")
    protected PartyAndSignature3 dgtlSgntr;
    @XmlElement(name = "Nclsr", required = true)
    protected byte[] nclsr;

    /**
     * Obtient la valeur de la propriété tp.
     * 
     * @return
     *     possible object is
     *     {@link DocumentType1Choice }
     *     
     */
    public DocumentType1Choice getTp() {
        return tp;
    }

    /**
     * Définit la valeur de la propriété tp.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentType1Choice }
     *     
     */
    public void setTp(DocumentType1Choice value) {
        this.tp = value;
    }

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
     * Obtient la valeur de la propriété isseDt.
     * 
     * @return
     *     possible object is
     *     {@link DateAndDateTime2Choice }
     *     
     */
    public DateAndDateTime2Choice getIsseDt() {
        return isseDt;
    }

    /**
     * Définit la valeur de la propriété isseDt.
     * 
     * @param value
     *     allowed object is
     *     {@link DateAndDateTime2Choice }
     *     
     */
    public void setIsseDt(DateAndDateTime2Choice value) {
        this.isseDt = value;
    }

    /**
     * Obtient la valeur de la propriété nm.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNm() {
        return nm;
    }

    /**
     * Définit la valeur de la propriété nm.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNm(String value) {
        this.nm = value;
    }

    /**
     * Obtient la valeur de la propriété langCd.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLangCd() {
        return langCd;
    }

    /**
     * Définit la valeur de la propriété langCd.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLangCd(String value) {
        this.langCd = value;
    }

    /**
     * Obtient la valeur de la propriété frmt.
     * 
     * @return
     *     possible object is
     *     {@link DocumentFormat1Choice }
     *     
     */
    public DocumentFormat1Choice getFrmt() {
        return frmt;
    }

    /**
     * Définit la valeur de la propriété frmt.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentFormat1Choice }
     *     
     */
    public void setFrmt(DocumentFormat1Choice value) {
        this.frmt = value;
    }

    /**
     * Obtient la valeur de la propriété fileNm.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileNm() {
        return fileNm;
    }

    /**
     * Définit la valeur de la propriété fileNm.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileNm(String value) {
        this.fileNm = value;
    }

    /**
     * Obtient la valeur de la propriété dgtlSgntr.
     * 
     * @return
     *     possible object is
     *     {@link PartyAndSignature3 }
     *     
     */
    public PartyAndSignature3 getDgtlSgntr() {
        return dgtlSgntr;
    }

    /**
     * Définit la valeur de la propriété dgtlSgntr.
     * 
     * @param value
     *     allowed object is
     *     {@link PartyAndSignature3 }
     *     
     */
    public void setDgtlSgntr(PartyAndSignature3 value) {
        this.dgtlSgntr = value;
    }

    /**
     * Obtient la valeur de la propriété nclsr.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getNclsr() {
        return nclsr;
    }

    /**
     * Définit la valeur de la propriété nclsr.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setNclsr(byte[] value) {
        this.nclsr = value;
    }

}
