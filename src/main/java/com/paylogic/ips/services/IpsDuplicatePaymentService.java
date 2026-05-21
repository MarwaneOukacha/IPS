package com.paylogic.ips.services;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.WebInterface;
import com.gms.utils.net.webinterface.WebRequest;
import com.gms.utils.net.webinterface.WebRequest.QUERY_METHOD;
import com.paylogic.ips.bo.IpsDuplicatePaymentRequestBo;
import com.paylogic.ips.bo.IpsReturnPaymentRequestBo;
import com.paylogic.ips.bo.IpsSendPaymentRequestBo;


@Service
public class IpsDuplicatePaymentService {

    private static final Logger LOG = Logger.getLogger(IpsDuplicatePaymentService.class);
    private static final String IPS_RECEIVER = "BRBUBIBAXIPS";
    private static final String OUR_BIC_SHORT = "BKGFBIBI";
    @Autowired
    private TokenService tokenService;

    @Value("${walletcore.url.acceptType:application/json}")
    private String acceptType;

    @Value("${walletcore.url.connectTimeout:10000}")
    private int connectTimeout;

    @Value("${walletcore.url.readTimeout:10000}")
    private int readTimeout;
    @Value("${ips.incoming.input.url}")
    private String ipsInputUrl;

    @Value("${user}")
    private String user;
    
    @Autowired
    private SignerService signerService;

    private KeyStore keyStore;
    @Value("${ips.signing.keystoreFile}")
    private String keystoreFile;

    @Value("${ips.signing.keystorePass}")
    private String keystorePass;

    @Value("${ips.signing.keyAlias}")
    private String keyAlias;

    @Value("${ips.signing.keyPass:}")
    private String keyPass;
    
    
    @PostConstruct
    private void initKeyStore() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(
                Files.newInputStream(Paths.get(keystoreFile)),
                keystorePass.toCharArray()
            );
            this.keyStore = ks;
            LOG.info("IPS keystore loaded OK - alias: " + keyAlias);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load IPS keystore", e);
        }
    }

    public void sendCamt033ToIPS(IpsDuplicatePaymentRequestBo requestBo)
            throws BusinessException {

        String requestUuid = UUID.randomUUID().toString();

        IpsSendPaymentRequestBo request = buildCamt033Request(requestBo);

        String url = ipsInputUrl + requestUuid + "?service=ips";

        LOG.info("Calling IPS input URL: " + url);
        LOG.info("IPS camt.033 request body: " + request);

        WebRequest webRequest = buildPostJsonRequest(url, request);

        executeRequest(webRequest, "IPS input");

        int responseCode = webRequest.getResponse().getResponseCode();
        String responseBody = webRequest.getResponse().getResponseMsg();

        LOG.info("IPS input – HTTP " + responseCode + " | body: " + responseBody);

        

        if (responseCode != 200 && responseCode != 201) {
        	JSONObject jsonResponse = new JSONObject(responseBody);
            throw new BusinessException(
                    jsonResponse.optString("description")
            );
        }

        logParsedJson(responseBody, "IPS input response");
    }
    
    public void sendPacs004ToIPS(IpsReturnPaymentRequestBo requestBo)
            throws BusinessException {

        String requestUuid = UUID.randomUUID().toString();

        IpsSendPaymentRequestBo request = buildPacs004Request(requestBo);

        String url = ipsInputUrl + requestUuid + "?service=ips";

        LOG.info("Calling IPS input URL: " + url);
        LOG.info("IPS pacs.004 request body: " + request);

        WebRequest webRequest = buildPostJsonRequest(url, request);

        executeRequest(webRequest, "IPS input");

        int responseCode = webRequest.getResponse().getResponseCode();
        String responseBody = webRequest.getResponse().getResponseMsg();

        LOG.info("IPS input – HTTP " + responseCode + " | body: " + responseBody);

        

        if (responseCode != 200 && responseCode != 201) {
        	JSONObject jsonResponse = new JSONObject(responseBody);

            throw new BusinessException(
                    jsonResponse.optString("description")
            );
        }

        logParsedJson(responseBody, "IPS input response");
    }
    
    
    
    private String buildPacs004Xml(
            IpsReturnPaymentRequestBo bo,
            String creationDateTime,
            String settlementDate) {

        return new StringBuilder()

            .append("<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">")

                .append("<Body>")

                    .append("<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">")

                        .append("<Fr>")
                            .append("<FIId>")
                                .append("<FinInstnId>")
                                    .append("<BICFI>").append(bo.getCreditorBic()).append("</BICFI>")
                                .append("</FinInstnId>")
                            .append("</FIId>")
                        .append("</Fr>")

                        .append("<To>")
                            .append("<FIId>")
                                .append("<FinInstnId>")
                                    .append("<BICFI>BRBUBIBI</BICFI>")
                                .append("</FinInstnId>")
                            .append("</FIId>")
                        .append("</To>")

                        .append("<BizMsgIdr>").append(bo.getTraceReference()).append("</BizMsgIdr>")
                        .append("<MsgDefIdr>pacs.004.001.11</MsgDefIdr>")
                        .append("<BizSvc>brb.ips.01</BizSvc>")
                        .append("<CreDt>").append(creationDateTime).append("</CreDt>")
                        .append("<Prty>0100</Prty>")

                    .append("</AppHdr>")

                    .append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.004.001.11\">")

                        .append("<PmtRtr>")

                            .append("<GrpHdr>")
                                .append("<MsgId>").append(bo.getTraceReference()).append("</MsgId>")
                                .append("<CreDtTm>").append(creationDateTime).append("</CreDtTm>")
                                .append("<NbOfTxs>1</NbOfTxs>")
                                .append("<SttlmInf>")
                                    .append("<SttlmMtd>CLRG</SttlmMtd>")
                                .append("</SttlmInf>")
                            .append("</GrpHdr>")

                            .append("<OrgnlGrpInf>")
                                .append("<OrgnlMsgId>").append(bo.getOriginalPaymentReference()).append("</OrgnlMsgId>")
                                .append("<OrgnlMsgNmId>pacs.008.001.10</OrgnlMsgNmId>")
                            .append("</OrgnlGrpInf>")

                            .append("<TxInf>")

                                .append("<RtrId>").append(bo.getTraceReference()).append("</RtrId>")
                                .append("<OrgnlInstrId>").append(bo.getOriginalPaymentReference()).append("</OrgnlInstrId>")
                                .append("<OrgnlEndToEndId>NOTPROVIDED</OrgnlEndToEndId>")
                                .append("<OrgnlTxId>").append(bo.getOriginalPaymentReference()).append("</OrgnlTxId>")
                                .append("<OrgnlIntrBkSttlmDt>").append(bo.getOriginalPaymentValueDate()).append("</OrgnlIntrBkSttlmDt>")

                                .append("<PmtTpInf>")
                                    .append("<ClrChanl>RTNS</ClrChanl>")
                                    .append("<LclInstrm>")
                                        .append("<Prtry>TRFI</Prtry>")
                                    .append("</LclInstrm>")
                                .append("</PmtTpInf>")

                                .append("<RtrdIntrBkSttlmAmt Ccy=\"BIF\">").append(bo.getRequestedAmount()).append("</RtrdIntrBkSttlmAmt>")
                                .append("<IntrBkSttlmDt>").append(settlementDate).append("</IntrBkSttlmDt>")
                                .append("<ChrgBr>SLEV</ChrgBr>")

                                .append("<InstgAgt>")
                                    .append("<FinInstnId>")
                                        .append("<BICFI>").append(bo.getCreditorBic()).append("</BICFI>")
                                    .append("</FinInstnId>")
                                .append("</InstgAgt>")

                                .append("<InstdAgt>")
                                    .append("<FinInstnId>")
                                        .append("<BICFI>").append(bo.getOriginalDebtorBic()).append("</BICFI>")
                                    .append("</FinInstnId>")
                                .append("</InstdAgt>")

                                .append("<RtrRsnInf>")
                                    .append("<Rsn>")
                                        .append("<Prtry>MS03</Prtry>")
                                    .append("</Rsn>")
                                    .append("<AddtlInf>NotSpecifiedReasonAgentGenerated</AddtlInf>")
                                .append("</RtrRsnInf>")

                                .append("<OrgnlTxRef>")

                                    .append("<IntrBkSttlmAmt Ccy=\"BIF\">").append(bo.getRequestedAmount()).append("</IntrBkSttlmAmt>")

                                    .append("<PmtTpInf>")
                                        .append("<ClrChanl>RTNS</ClrChanl>")
                                        .append("<LclInstrm>")
                                            .append("<Prtry>TRFI</Prtry>")
                                        .append("</LclInstrm>")
                                    .append("</PmtTpInf>")

                                    .append("<RmtInf>")
                                        .append("<Ustrd>").append(bo.getRemittanceInfo()).append("</Ustrd>")
                                    .append("</RmtInf>")

                                    .append("<Dbtr>")
                                        .append("<Pty>")
                                            .append("<Nm>").append(bo.getDebtorName()).append("</Nm>")
                                        .append("</Pty>")
                                    .append("</Dbtr>")

                                    .append("<DbtrAcct>")
                                        .append("<Id>")
                                            .append("<Othr>")
                                                .append("<Id>").append(bo.getDebtorAccount()).append("</Id>")
                                            .append("</Othr>")
                                        .append("</Id>")
                                    .append("</DbtrAcct>")

                                    .append("<DbtrAgt>")
                                        .append("<FinInstnId>")
                                            .append("<BICFI>").append(bo.getOriginalDebtorBic()).append("</BICFI>")
                                        .append("</FinInstnId>")
                                    .append("</DbtrAgt>")

                                    .append("<CdtrAgt>")
                                        .append("<FinInstnId>")
                                            .append("<BICFI>").append(bo.getCreditorBic()).append("</BICFI>")
                                        .append("</FinInstnId>")
                                    .append("</CdtrAgt>")

                                    .append("<Cdtr>")
                                        .append("<Pty>")
                                            .append("<Nm>").append(bo.getCreditorName()).append("</Nm>")
                                        .append("</Pty>")
                                    .append("</Cdtr>")

                                    .append("<CdtrAcct>")
                                        .append("<Id>")
                                            .append("<Othr>")
                                                .append("<Id>").append(bo.getCreditorAccount()).append("</Id>")
                                            .append("</Othr>")
                                        .append("</Id>")
                                    .append("</CdtrAcct>")

                                    .append("<Purp>")
                                        .append("<Prtry>").append(bo.getPurposeCode()).append("</Prtry>")
                                    .append("</Purp>")

                                .append("</OrgnlTxRef>")

                            .append("</TxInf>")

                        .append("</PmtRtr>")

                    .append("</Document>")

                .append("</Body>")

            .append("</DataPDU>")

            .toString();
    }
    
    private IpsSendPaymentRequestBo buildCamt033Request(IpsDuplicatePaymentRequestBo requestBo)
            throws BusinessException {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("+02:00"));

        String creationDateTime = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        validateRequired(requestBo.getTraceReference(), "traceReference");
        validateRequired(requestBo.getOriginalPaymentReference(), "originalPaymentReference");
        validateRequired(requestBo.getOriginalPaymentValueDate(), "originalPaymentValueDate");
        validateRequired(requestBo.getOriginalDebitorBic(), "originalDebitorBic");

        String xml = buildCamt033Xml(
                requestBo.getTraceReference(),
                creationDateTime,
                requestBo.getOriginalPaymentReference(),
                requestBo.getOriginalPaymentValueDate(),
                requestBo.getOriginalDebitorBic()
        );
        String signedXml;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(
                new InputSource(new StringReader(xml.toString()))
            );

  
            String pass = (keyPass != null && !keyPass.isEmpty())
                    ? keyPass
                    : keystorePass;
            PrivateKey privateKey = (PrivateKey)      keyStore.getKey(keyAlias, pass.toCharArray());
            X509Certificate cert  = (X509Certificate) keyStore.getCertificate(keyAlias);
            

            Document signedDoc = signerService.sign(doc, privateKey, cert);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(signedDoc), new StreamResult(writer));
            signedXml = writer.toString();

        } catch (Exception e) {
            LOG.error("Failed to sign document", e);
            throw new BusinessException("Failed to sign payment XML: " + e.getMessage(), e);
        }
        IpsSendPaymentRequestBo request = new IpsSendPaymentRequestBo();

        request.setTraceReference(requestBo.getTraceReference());
        request.setType("camt.033.001.06");
        request.setSender(user);
        request.setReceiver(IPS_RECEIVER);
        request.setDocument(signedXml);

        return request;
    }
    
    private IpsSendPaymentRequestBo buildPacs004Request(
            IpsReturnPaymentRequestBo requestBo)
            throws BusinessException {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("+02:00"));

        String creationDateTime =
                now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String settlementDate =
                now.toLocalDate().toString();

        validateRequired(requestBo.getTraceReference(), "traceReference");
        validateRequired(requestBo.getOriginalPaymentReference(), "originalPaymentReference");
        validateRequired(requestBo.getOriginalPaymentValueDate(), "originalPaymentValueDate");
        validateRequired(requestBo.getRequestedAmount(), "requestedAmount");
        String xml = buildPacs004Xml(
        		requestBo,
                creationDateTime,
                settlementDate
                
        );
        
        String signedXml;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(
                new InputSource(new StringReader(xml.toString()))
            );

  
            String pass = (keyPass != null && !keyPass.isEmpty())
                    ? keyPass
                    : keystorePass;
            PrivateKey privateKey = (PrivateKey)      keyStore.getKey(keyAlias, pass.toCharArray());
            X509Certificate cert  = (X509Certificate) keyStore.getCertificate(keyAlias);
            

            Document signedDoc = signerService.sign(doc, privateKey, cert);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(signedDoc), new StreamResult(writer));
            signedXml = writer.toString();

        } catch (Exception e) {
            LOG.error("Failed to sign document", e);
            throw new BusinessException("Failed to sign payment XML: " + e.getMessage(), e);
        }

        IpsSendPaymentRequestBo request =
                new IpsSendPaymentRequestBo();

        request.setTraceReference(requestBo.getTraceReference());
        request.setType("pacs.004.001.11");
        request.setSender(user);
        request.setReceiver(IPS_RECEIVER);
        request.setDocument(signedXml);

        return request;
    }
/*
    private IpsSendPaymentRequestBo buildCamt033Request(IpsDuplicatePaymentRequestBo requestBo)
            throws BusinessException {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("+02:00"));

        String creationDateTime = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        validateRequired(requestBo.getTraceReference(), "traceReference");
        validateRequired(requestBo.getOriginalPaymentReference(), "originalPaymentReference");
        validateRequired(requestBo.getOriginalPaymentValueDate(), "originalPaymentValueDate");
        validateRequired(requestBo.getOriginalDebitorBic(), "originalDebitorBic");

        String xml = buildCamt033Xml(
                requestBo.getTraceReference(),
                creationDateTime,
                requestBo.getOriginalPaymentReference(),
                requestBo.getOriginalPaymentValueDate(),
                requestBo.getOriginalDebitorBic()
        );

        IpsSendPaymentRequestBo request = new IpsSendPaymentRequestBo();

        request.setTraceReference(requestBo.getTraceReference());
        request.setType("camt.033.001.06");
        request.setSender(user);
        request.setReceiver(IPS_RECEIVER);
        request.setDocument(xml);

        return request;
    }
    
    private IpsSendPaymentRequestBo buildPacs004Request(
            IpsReturnPaymentRequestBo requestBo)
            throws BusinessException {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("+02:00"));

        String creationDateTime =
                now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String settlementDate =
                now.toLocalDate().toString();

        validateRequired(requestBo.getTraceReference(), "traceReference");
        validateRequired(requestBo.getOriginalPaymentReference(), "originalPaymentReference");
        validateRequired(requestBo.getOriginalPaymentValueDate(), "originalPaymentValueDate");
        validateRequired(requestBo.getRequestedAmount(), "requestedAmount");
        String xml = buildPacs004Xml(
        		requestBo,
                creationDateTime,
                settlementDate
                
        );
        
        

        IpsSendPaymentRequestBo request =
                new IpsSendPaymentRequestBo();

        request.setTraceReference(requestBo.getTraceReference());
        request.setType("pacs.004.001.11");
        request.setSender(user);
        request.setReceiver(IPS_RECEIVER);
        request.setDocument(xml);

        return request;
    }
*/
    private String buildCamt033Xml(
            String traceReference,
            String creationDateTime,
            String originalPaymentReference,
            String originalPaymentValueDate,
            String originalDebtorBic) {

        return new StringBuilder()

            .append("<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">")

                .append("<Body>")

                    .append("<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">")

                        .append("<Fr>")
                            .append("<FIId>")
                                .append("<FinInstnId>")
                                    .append("<BICFI>").append(OUR_BIC_SHORT).append("</BICFI>")
                                .append("</FinInstnId>")
                            .append("</FIId>")
                        .append("</Fr>")

                        .append("<To>")
                            .append("<FIId>")
                                .append("<FinInstnId>")
                                    .append("<BICFI>BRBUBIBI</BICFI>")
                                .append("</FinInstnId>")
                            .append("</FIId>")
                        .append("</To>")

                        .append("<BizMsgIdr>").append(traceReference).append("</BizMsgIdr>")
                        .append("<MsgDefIdr>camt.033.001.06</MsgDefIdr>")
                        .append("<BizSvc>brb.ips.01</BizSvc>")
                        .append("<CreDt>").append(creationDateTime).append("</CreDt>")

                    .append("</AppHdr>")

                    .append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.033.001.06\">")

                        .append("<ReqForDplct>")

                            .append("<Assgnmt>")

                                .append("<Id>").append(traceReference).append("</Id>")

                                .append("<Assgnr>")
                                    .append("<Agt>")
                                        .append("<FinInstnId>")
                                            .append("<BICFI>").append(OUR_BIC_SHORT).append("</BICFI>")
                                        .append("</FinInstnId>")
                                    .append("</Agt>")
                                .append("</Assgnr>")

                                .append("<Assgne>")
                                    .append("<Agt>")
                                        .append("<FinInstnId>")
                                            .append("<BICFI>BRBUBIBI</BICFI>")
                                        .append("</FinInstnId>")
                                    .append("</Agt>")
                                .append("</Assgne>")

                                .append("<CreDtTm>").append(creationDateTime).append("</CreDtTm>")

                            .append("</Assgnmt>")

                            .append("<Case>")

                                .append("<Id>").append(traceReference).append("</Id>")

                                .append("<Cretr>")
                                    .append("<Agt>")
                                        .append("<FinInstnId>")
                                            .append("<BICFI>").append(OUR_BIC_SHORT).append("</BICFI>")
                                        .append("</FinInstnId>")
                                    .append("</Agt>")
                                .append("</Cretr>")

                            .append("</Case>")

                            .append("<SplmtryData>")

                                .append("<PlcAndNm>cma.paymentSearch.001.02</PlcAndNm>")

                                .append("<Envlp>")

                                    .append("<Document xmlns=\"urn:cma:xsd:cma.paymentSearch.001.02\">")

                                        .append("<PmtSch>")

                                            .append("<MsgId>")
                                                .append(originalPaymentReference)
                                            .append("</MsgId>")

                                            .append("<ShrtBizId>")

                                                .append("<IntrBkSttlmDt>")
                                                    .append(originalPaymentValueDate)
                                                .append("</IntrBkSttlmDt>")

                                                .append("<InstgAgt>")
                                                    .append("<FinInstnId>")
                                                        .append("<BICFI>")
                                                            .append(originalDebtorBic)
                                                        .append("</BICFI>")
                                                    .append("</FinInstnId>")
                                                .append("</InstgAgt>")

                                            .append("</ShrtBizId>")

                                        .append("</PmtSch>")

                                    .append("</Document>")

                                .append("</Envlp>")

                            .append("</SplmtryData>")

                        .append("</ReqForDplct>")

                    .append("</Document>")

                .append("</Body>")

            .append("</DataPDU>")

            .toString();
    }

    private void validateRequired(String value, String fieldName)
            throws BusinessException {

        if (value == null || value.trim().isEmpty()) {

            throw new BusinessException(fieldName + " is required");
        }
    }
    
    private WebRequest buildPostJsonRequest(String url, Object body) throws BusinessException {
        WebRequest req = baseRequest(url);
        req.setQueryMethod(QUERY_METHOD.POST);
        req.setMediaType("application/json");
        req.setBody(body);
        return req;
    }

    private WebRequest baseRequest(String url) throws BusinessException {
        WebRequest req = new WebRequest();
        req.setUrl(url);
        req.setAcceptType("application/json");
        req.setReadTimeout(readTimeout);
        req.setConnectTimeout(connectTimeout);
        req.setHeader(tokenService.buildBearerHeader(tokenService.getAccessToken()));
        return req;
    }

    private void executeRequest(WebRequest webRequest, String label)
            throws BusinessException {
        try {
            WebInterface.processRequest(webRequest);
        } catch (IOException e) {
            LOG.error("IO error calling " + label, e);
            throw new BusinessException("Failed to call " + label + " endpoint", e);
        }
    }
    private void logParsedJson(String responseBody, String label) throws BusinessException {
        try {
            JsonNode json = new ObjectMapper().readTree(responseBody);
            LOG.info("Parsed " + label + ": " + json);
        } catch (Exception e) {
            LOG.error("Failed to parse " + label, e);
            throw new BusinessException("Failed to parse " + label, e);
        }
    }
}