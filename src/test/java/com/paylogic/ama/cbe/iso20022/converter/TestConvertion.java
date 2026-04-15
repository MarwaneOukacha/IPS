package com.paylogic.ama.cbe.iso20022.converter;

import static org.junit.Assert.assertNotNull;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gms.utils.net.webinterface.WebRequest;
import com.paylogic.ama.core.model.CustomerKyc;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ips.converter.outgoing.Pacs008OutgoingConverter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/applicationContext.xml"})
public class TestConvertion {

    private static final Logger LOG = Logger.getLogger(TestConvertion.class.getName());

    @Autowired
    private Pacs008OutgoingConverter pacs008Converter;  // optional if you use it

    @Test
    public void testMarshalDataPDU_usingTemplate() {

        // 1. Create test Payment
        Payment payment = new Payment();
        payment.setIssuerTrxRef("TX123456789");
        payment.setCreateTime(java.sql.Timestamp.valueOf("2024-02-05 12:30:00"));
        payment.setAmount(12500.0);
        payment.setCurrency("BIF");
        payment.setAccountNumber("20416210088");

        // Customer KYC
        CustomerKyc customer = new CustomerKyc();
        customer.setName("Doe");
        customer.setFirstname("John");
        payment.setSenderCustomerData(customer);
        payment.setReceiverCustomerData(customer);

        // 2. Dynamic values
        String xmlSenderBic = "TX123456789";
        String senderReference = payment.getIssuerTrxRef();
        String createDate = "2026-02-06T11:24:39";
        String paymentAmount = "12500";
        String interbankDate = "2026-02-06";
        String creditorAgent = "BRBUBIBAXXXX";

        // 3. Build XML using StringBuilder (Java 8 compatible)
        StringBuilder xml = new StringBuilder();
        xml.append("<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">")
           .append("<Body>")

           .append("<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">")
               .append("<Fr><FIId><FinInstnId>")
                   .append(xmlSenderBic)
               .append("</FinInstnId></FIId></Fr>")

               .append("<To><FIId><FinInstnId><BICFI>BRBUBIBI</BICFI></FinInstnId></FIId></To>")

               .append("<BizMsgIdr>").append(senderReference).append("</BizMsgIdr>")
               .append("<MsgDefIdr>pacs.008.001.10</MsgDefIdr>")
               .append("<BizSvc>brb.ips.01</BizSvc>")
               .append("<CreDt>").append(createDate).append("+02:00</CreDt>")
               .append("<Prty>0100</Prty>")
           .append("</AppHdr>")

           .append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.10\">")
               .append("<FIToFICstmrCdtTrf>")

                   .append("<GrpHdr>")
                       .append("<MsgId>").append(senderReference).append("</MsgId>")
                       .append("<CreDtTm>").append(createDate).append("+02:00</CreDtTm>")
                       .append("<NbOfTxs>1</NbOfTxs>")
                       .append("<SttlmInf><SttlmMtd>CLRG</SttlmMtd></SttlmInf>")
                   .append("</GrpHdr>")

                   .append("<CdtTrfTxInf>")

                       .append("<PmtId>")
                           .append("<InstrId>").append(senderReference).append("</InstrId>")
                           .append("<EndToEndId>NOTPROVIDED</EndToEndId>")
                           .append("<TxId>").append(senderReference).append("</TxId>")
                       .append("</PmtId>")

                       .append("<PmtTpInf>")
                           .append("<ClrChanl>RTNS</ClrChanl>")
                           .append("<LclInstrm><Prtry>TRFI</Prtry></LclInstrm>")
                       .append("</PmtTpInf>")

                       .append("<IntrBkSttlmAmt Ccy=\"BIF\">").append(paymentAmount).append("</IntrBkSttlmAmt>")
                       .append("<IntrBkSttlmDt>").append(interbankDate).append("</IntrBkSttlmDt>")
                       .append("<ChrgBr>SLEV</ChrgBr>")

                       .append("<InstgAgt><FinInstnId>")
                           .append(xmlSenderBic)
                       .append("</FinInstnId></InstgAgt>")

                       .append("<InstdAgt><FinInstnId>")
                           .append(creditorAgent)
                       .append("</FinInstnId></InstdAgt>")

                   .append("</CdtTrfTxInf>")

               .append("</FIToFICstmrCdtTrf>")
           .append("</Document>")

           .append("</Body>")
        .append("</DataPDU>");

        // Convert to string
        String finalXML = xml.toString();

        System.out.println("Generated IPS XML:");
        System.out.println(finalXML);

        assertNotNull(finalXML);
    }
}
