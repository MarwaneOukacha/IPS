//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2023.10.29 à 11:22:35 AM CAT 
//


package com.paylogic.ips.iso20022.bo.pacs003;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import com.paylogic.ips.iso20022.bo.pacs.PacsBaseDocument;



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
 *         &lt;element name="FIToFICstmrDrctDbt" type="{urn:iso:std:iso:20022:tech:xsd:pacs.003.001.10}FIToFICustomerDirectDebitV10"/&gt;
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
@XmlType(name = "DocumentPacs003", propOrder = {
    "fiToFICstmrDrctDbt"
})
public class DocumentPacs003 extends PacsBaseDocument{

    @XmlElement(name = "FIToFICstmrDrctDbt", required = true)
    protected FIToFICustomerDirectDebitV10 fiToFICstmrDrctDbt;

    /**
     * Obtient la valeur de la propriété fiToFICstmrDrctDbt.
     * 
     * @return
     *     possible object is
     *     {@link FIToFICustomerDirectDebitV10 }
     *     
     */
    public FIToFICustomerDirectDebitV10 getFIToFICstmrDrctDbt() {
        return fiToFICstmrDrctDbt;
    }

    /**
     * Définit la valeur de la propriété fiToFICstmrDrctDbt.
     * 
     * @param value
     *     allowed object is
     *     {@link FIToFICustomerDirectDebitV10 }
     *     
     */
    public void setFIToFICstmrDrctDbt(FIToFICustomerDirectDebitV10 value) {
        this.fiToFICstmrDrctDbt = value;
    }
    @Override
	protected void initVersion() {
		functionality = "pacs";
		variant = "003";
		version = "001.10";
	}

	@Override
	public String getMsgId() {
		return fiToFICstmrDrctDbt.grpHdr.msgId;
	}

	@Override
	public XMLGregorianCalendar getCreDtTm() {
		return fiToFICstmrDrctDbt.grpHdr.creDtTm;
	}
	
	@Override
	public String getInstgAgtId() {
		if(fiToFICstmrDrctDbt.grpHdr.instgAgt != null && fiToFICstmrDrctDbt.grpHdr.instgAgt.finInstnId != null && fiToFICstmrDrctDbt.grpHdr.instgAgt.finInstnId.othr != null) {
			return fiToFICstmrDrctDbt.grpHdr.instgAgt.finInstnId.othr.id;
		}else {
			return null;
		}
	}
	
	@Override
	public String getInstdAgtId() {
		if(fiToFICstmrDrctDbt.grpHdr.instdAgt != null && fiToFICstmrDrctDbt.grpHdr.instdAgt.finInstnId != null && fiToFICstmrDrctDbt.grpHdr.instdAgt.finInstnId.othr != null) {
			return fiToFICstmrDrctDbt.grpHdr.instdAgt.finInstnId.othr.id;
		}else {
			return null;
		}
	}
	
	@Override
	public String getTransactionId() {
		if(fiToFICstmrDrctDbt.drctDbtTxInf != null && !fiToFICstmrDrctDbt.drctDbtTxInf.isEmpty() && fiToFICstmrDrctDbt.drctDbtTxInf.get(0).pmtId != null) {
			return fiToFICstmrDrctDbt.drctDbtTxInf.get(0).pmtId.txId;
		}else {
			return null;
		}
	}
	
	@Override
	public String getClearingSysRef() {
		if(fiToFICstmrDrctDbt.drctDbtTxInf != null && !fiToFICstmrDrctDbt.drctDbtTxInf.isEmpty() && fiToFICstmrDrctDbt.drctDbtTxInf.get(0).pmtId != null) {
			return fiToFICstmrDrctDbt.drctDbtTxInf.get(0).pmtId.clrSysRef;
		}else {
			return null;
		}
	}
	
	@Override
	public BigDecimal getTransactionAmount() {
		if(fiToFICstmrDrctDbt.drctDbtTxInf != null && !fiToFICstmrDrctDbt.drctDbtTxInf.isEmpty() && fiToFICstmrDrctDbt.drctDbtTxInf.get(0).instdAmt != null) {
			return fiToFICstmrDrctDbt.drctDbtTxInf.get(0).instdAmt.value;
		}else {
			return null;
		}
	}
	
	@Override
	public String getTransactionCurrency() {
		if(fiToFICstmrDrctDbt.drctDbtTxInf != null && !fiToFICstmrDrctDbt.drctDbtTxInf.isEmpty() && fiToFICstmrDrctDbt.drctDbtTxInf.get(0).instdAmt != null) {
			return fiToFICstmrDrctDbt.drctDbtTxInf.get(0).instdAmt.ccy;
		}else {
			return null;
		}
	}
	
	@Override
	public BigDecimal getInterBankAmount() {
		if(fiToFICstmrDrctDbt.drctDbtTxInf != null && !fiToFICstmrDrctDbt.drctDbtTxInf.isEmpty() && fiToFICstmrDrctDbt.drctDbtTxInf.get(0).intrBkSttlmAmt != null) {
			return fiToFICstmrDrctDbt.drctDbtTxInf.get(0).intrBkSttlmAmt.value;
		}else {
			return null;
		}
	}
	
	@Override
	public String getInterBankCurrency() {
		if(fiToFICstmrDrctDbt.drctDbtTxInf != null && !fiToFICstmrDrctDbt.drctDbtTxInf.isEmpty() && fiToFICstmrDrctDbt.drctDbtTxInf.get(0).intrBkSttlmAmt != null) {
			return fiToFICstmrDrctDbt.drctDbtTxInf.get(0).intrBkSttlmAmt.ccy;
		}else {
			return null;
		}
	}
	
	@Override
	public String getDebtorAccountId() {
		if(fiToFICstmrDrctDbt.drctDbtTxInf != null && !fiToFICstmrDrctDbt.drctDbtTxInf.isEmpty() && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).dbtrAcct != null && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).dbtrAcct.id != null && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).dbtrAcct.id.othr != null) {
			return fiToFICstmrDrctDbt.drctDbtTxInf.get(0).dbtrAcct.id.othr.id;
		}else {
			return null;
		}
	}
	
	@Override
	public String getDebtorAccountScheme() {
		if(fiToFICstmrDrctDbt.drctDbtTxInf != null && !fiToFICstmrDrctDbt.drctDbtTxInf.isEmpty() && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).dbtrAcct != null && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).dbtrAcct.id != null && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).dbtrAcct.id.othr != null&& 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).dbtrAcct.id.othr.schmeNm != null) {
			return fiToFICstmrDrctDbt.drctDbtTxInf.get(0).dbtrAcct.id.othr.schmeNm.prtry;
		}else {
			return null;
		}
	}
	
	@Override
	public String getDebtorAccountCurrency() {
		if(fiToFICstmrDrctDbt.drctDbtTxInf != null && !fiToFICstmrDrctDbt.drctDbtTxInf.isEmpty() && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).dbtrAcct != null) {
			return fiToFICstmrDrctDbt.drctDbtTxInf.get(0).dbtrAcct.ccy;
		}else {
			return null;
		}
	}
	
	@Override
	public String getCreditorAccountId() {
		if(fiToFICstmrDrctDbt.drctDbtTxInf != null && !fiToFICstmrDrctDbt.drctDbtTxInf.isEmpty() && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).cdtrAcct != null && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).cdtrAcct.id != null && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).cdtrAcct.id.othr != null) {
			return fiToFICstmrDrctDbt.drctDbtTxInf.get(0).cdtrAcct.id.othr.id;
		}else {
			return null;
		}
	}
	
	@Override
	public String getCreditorAccountScheme() {
		if(fiToFICstmrDrctDbt.drctDbtTxInf != null && !fiToFICstmrDrctDbt.drctDbtTxInf.isEmpty() && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).cdtrAcct != null && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).cdtrAcct.id != null && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).cdtrAcct.id.othr != null&& 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).cdtrAcct.id.othr.schmeNm != null) {
			return fiToFICstmrDrctDbt.drctDbtTxInf.get(0).cdtrAcct.id.othr.schmeNm.prtry;
		}else {
			return null;
		}
	}
	
	@Override
	public String getCreditorAccountCurrency() {
		if(fiToFICstmrDrctDbt.drctDbtTxInf != null && !fiToFICstmrDrctDbt.drctDbtTxInf.isEmpty() && 
		   fiToFICstmrDrctDbt.drctDbtTxInf.get(0).cdtrAcct != null) {
			return fiToFICstmrDrctDbt.drctDbtTxInf.get(0).cdtrAcct.ccy;
		}else {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "DocumentPacs003 {'fiToFICstmrDrctDbt':'" + fiToFICstmrDrctDbt + "'}";
	}

}
