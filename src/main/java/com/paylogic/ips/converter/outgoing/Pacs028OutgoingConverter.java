package com.paylogic.ips.converter.outgoing;

import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gms.utils.common.RandomUtil;
import com.paylogic.ama.core.model.ParameterCategory;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ips.converter.DocumentConverter;
import com.paylogic.ips.iso20022.bo.pacs028.BranchAndFinancialInstitutionIdentification6;
import com.paylogic.ips.iso20022.bo.pacs028.DocumentPacs028;
import com.paylogic.ips.iso20022.bo.pacs028.FIToFIPaymentStatusRequestV05;
import com.paylogic.ips.iso20022.bo.pacs028.FinancialInstitutionIdentification18;
import com.paylogic.ips.iso20022.bo.pacs028.GenericFinancialIdentification1;
import com.paylogic.ips.iso20022.bo.pacs028.GroupHeader91;
import com.paylogic.ips.iso20022.bo.pacs028.PaymentTransaction131;
import com.paylogic.ips.store.IsoParameterStore;
import com.paylogic.ips.util.CoreUtil;

@Component
public class Pacs028OutgoingConverter extends DocumentConverter{
	private static final Logger LOG = Logger.getLogger(Pacs028OutgoingConverter.class.getName());
	@Autowired
	IsoParameterStore handlerStore;
	
	public DocumentPacs028 convertPayment(Payment payment,List<ParameterCategory> cats) {
		FIToFIPaymentStatusRequestV05 pacs028 = new FIToFIPaymentStatusRequestV05();
		//set header
		pacs028.setGrpHdr(createHeader(payment, cats));
		//set body
		pacs028.getTxInf().add(createPaymentTransaction(payment,cats));
		//create document
		DocumentPacs028 document = new DocumentPacs028();
		document.setFIToFIPmtStsReq(pacs028);
		//include message
		includeMessage(document, cats, payment, false);
		return document;
	}
	
	private GroupHeader91 createHeader(Payment payment, List<ParameterCategory> cats) {
		// setup header
		GroupHeader91 header = new GroupHeader91();
		header.setMsgId(RandomUtil.randomAlphaNumeric(12));
		try {
			header.setCreDtTm(CoreUtil.convertDateToXmlGregorienDate(payment.getCreateTime(),true));
		} catch (DatatypeConfigurationException ex) {
			LOG.info("createHeader::converting createTime error "+ex);
			header.setCreDtTm(null);
		}
		// set Instructing agent
		header.setInstgAgt(createFinancialInst(payment,cats,true));
		// set Instructed agent
		header.setInstdAgt(createFinancialInst(payment,cats,false));
		return header;
	}
	
	private BranchAndFinancialInstitutionIdentification6 createFinancialInst(Payment payment, List<ParameterCategory> cats, boolean isFrom) {
		BranchAndFinancialInstitutionIdentification6 inst = new BranchAndFinancialInstitutionIdentification6();
		FinancialInstitutionIdentification18 iden = new FinancialInstitutionIdentification18();
		GenericFinancialIdentification1 gfi = new GenericFinancialIdentification1(); 
		gfi.setId(isFrom ? payment.getFromMember() : payment.getToMember());
		iden.setOthr(gfi);
		inst.setFinInstnId(iden);
		return inst;
	}
	
	private PaymentTransaction131 createPaymentTransaction(Payment payment, List<ParameterCategory> cats) {
		PaymentTransaction131 trx = new PaymentTransaction131();
		trx.setOrgnlTxId(payment.getIssuerTrxRef());
		trx.setClrSysRef(payment.getVoucherCode());
		// set Instructing agent
		trx.setInstgAgt(createFinancialInst(payment,cats,true));
		// set Instructed agent
		trx.setInstdAgt(createFinancialInst(payment,cats,false));
		return trx;
	}
}
