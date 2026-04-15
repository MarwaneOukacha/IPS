//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2023.10.26 à 07:20:48 PM CAT 
//


package com.paylogic.ips.iso20022.bo.pacs002;

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
 *         &lt;element name="FIToFIPmtStsRpt" type="{urn:iso:std:iso:20022:tech:xsd:pacs.002.001.13}FIToFIPaymentStatusReportV13"/&gt;
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
@XmlType(name = "DocumentPacs002", propOrder = {
    "fiToFIPmtStsRpt"
})
public class DocumentPacs002 extends PacsBaseDocument{

    @XmlElement(name = "FIToFIPmtStsRpt", required = true)
    protected FIToFIPaymentStatusReportV13 fiToFIPmtStsRpt;
    public DocumentPacs002() {
		super();
	}
    /**
     * Obtient la valeur de la propriété fiToFIPmtStsRpt.
     * 
     * @return
     *     possible object is
     *     {@link FIToFIPaymentStatusReportV13 }
     *     
     */
    public FIToFIPaymentStatusReportV13 getFIToFIPmtStsRpt() {
        return fiToFIPmtStsRpt;
    }

    /**
     * Définit la valeur de la propriété fiToFIPmtStsRpt.
     * 
     * @param value
     *     allowed object is
     *     {@link FIToFIPaymentStatusReportV13 }
     *     
     */
    public void setFIToFIPmtStsRpt(FIToFIPaymentStatusReportV13 value) {
        this.fiToFIPmtStsRpt = value;
    }
    
    @Override
	protected void initVersion() {
		functionality = "pacs";
		variant = "002";
		version = "001.13";
	}

	@Override
	public String getMsgId() {
		return fiToFIPmtStsRpt.grpHdr.msgId; 
	}

	@Override
	public XMLGregorianCalendar getCreDtTm() {
		return fiToFIPmtStsRpt.grpHdr.creDtTm;
	}
	
	@Override
	public String getInstgAgtId() {
		if(fiToFIPmtStsRpt.grpHdr.instgAgt != null && fiToFIPmtStsRpt.grpHdr.instgAgt.finInstnId != null && fiToFIPmtStsRpt.grpHdr.instgAgt.finInstnId.othr != null) {
			return fiToFIPmtStsRpt.grpHdr.instgAgt.finInstnId.othr.id;
		}else {
			return null;
		}
	}
	
	@Override
	public String getInstdAgtId() {
		if(fiToFIPmtStsRpt.grpHdr.instdAgt != null && fiToFIPmtStsRpt.grpHdr.instdAgt.finInstnId != null && fiToFIPmtStsRpt.grpHdr.instdAgt.finInstnId.othr != null) {
			return fiToFIPmtStsRpt.grpHdr.instdAgt.finInstnId.othr.id;
		}else {
			return null;
		}
	}
	
	@Override
	public String getTransactionId() {
		if(fiToFIPmtStsRpt.txInfAndSts != null && !fiToFIPmtStsRpt.txInfAndSts.isEmpty()) {
			return fiToFIPmtStsRpt.txInfAndSts.get(0).orgnlTxId;
		}else {
			return null;
		}
	}
	
	@Override
	public String getClearingSysRef() {
		if(fiToFIPmtStsRpt.txInfAndSts != null && !fiToFIPmtStsRpt.txInfAndSts.isEmpty()) {
			return fiToFIPmtStsRpt.txInfAndSts.get(0).clrSysRef;
		}else {
			return null;
		}
	}

	@Override
	public BigDecimal getTransactionAmount() {
		return null;
	}
	
	@Override
	public String getTransactionCurrency() {
		return null;
	}
	
	@Override
	public BigDecimal getInterBankAmount() {
		return null;
	}
	
	@Override
	public String getInterBankCurrency() {
		return null;
	}
	
	@Override
	public String getDebtorAccountId() {
		return null;
	}
	
	@Override
	public String getDebtorAccountScheme() {
		return null;
	}

	@Override
	public String getDebtorAccountCurrency() {
		return null;
	}
	
	@Override
	public String getCreditorAccountId() {
		return null;
	}
	
	@Override
	public String getCreditorAccountScheme() {
		return null;
	}
	
	@Override
	public String getCreditorAccountCurrency() {
		return null;
	}

	@Override
	public String toString() {
		return "DocumentPacs002 {'fiToFIPmtStsRpt':'" + fiToFIPmtStsRpt + "'}";
	}
}
