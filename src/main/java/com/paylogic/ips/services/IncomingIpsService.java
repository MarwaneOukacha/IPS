package com.paylogic.ips.services;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gms.utils.common.StringUtil;
import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.WebInterface;
import com.gms.utils.net.webinterface.WebRequest;
import com.gms.utils.net.webinterface.WebRequest.QUERY_METHOD;
import com.paylogic.ama.core.model.AccountInfo;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ama.core.utils.BaseCoreUtil;
import com.paylogic.ama.wm.core.bo.EntityPayment;
import com.paylogic.ama.wm.core.bo.WalletPaymentBo;
import com.paylogic.ips.bo.IpsSendPaymentRequestBo;
import com.paylogic.ips.util.CoreUtil;

@Service
public class IncomingIpsService {

    private static final Logger LOG = Logger.getLogger(IncomingIpsService.class);

    private static final String OUR_BIC       = "BKGFBIBIXXXX";
    private static final String OUR_BIC_SHORT = "BKGFBIBI";       // used inside XML FinInstnId
    private static final String IPS_RECEIVER  = "BRBUBIBAXIPS";   // pacs.002 JSON receiver field

    @Autowired
    private TokenService tokenService;

    @Value("${walletcore.url.acceptType:application/json}")
    private String acceptType;

    @Value("${walletcore.url.connectTimeout:10000}")
    private int connectTimeout;

    @Value("${walletcore.url.mediaType:application/x-www-form-urlencoded}")
    private String mediaTypeForm;

    @Value("${notif.url.mediaType:application/json}")
    private String mediaTypeJson;

    @Value("${walletcore.url.readTimeout:10000}")
    private int readTimeout;

    @Value("${walletcore.url.gettoken}")
    private String urlToken;

    @Value("${ips.incoming.output.url}")
    private String ipsOutputUrl;

    @Value("${ips.incoming.input.url}")
    
    private String ipsInputUrl;

    @Value("${walletcore.url.incoming.create}")
    private String urlIncomingCreate;

    @Value("${ips.incoming.scheduler.enabled}")
    private boolean schedulerEnabled;
    private static final String PACS008_TYPE = "pacs.008.001.10";
    
    @Value("${ips.signing.keystoreFile}")
    private String keystoreFile;

    @Value("${ips.signing.keystorePass}")
    private String keystorePass;

    @Value("${ips.signing.keyAlias}")
    private String keyAlias;

    @Value("${ips.signing.keyPass:}")
    private String keyPass;
    @Autowired
    private SignerService signerService;

    private KeyStore keyStore;

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

    // =========================================================================
    // SCHEDULER
    // =========================================================================
   

    @Scheduled(fixedRateString = "${ips.incoming.scheduler.fixedRate:15000}")
    public void scheduledIncomingTransactionsPoll() {
        if (!schedulerEnabled) {
            LOG.debug("Incoming IPS scheduler is disabled");
            return;
        }
        LOG.info("SCHEDULED TASK: Polling IPS for incoming transactions");
        try {
            int processedCount = fetchAndProcessIncomingTransactions();
            if (processedCount > 0) {
                LOG.info("Scheduler successfully processed " + processedCount + " incoming transaction(s)");
            } else {
                LOG.info("Scheduler completed - No new incoming transactions found");
            }
        } catch (BusinessException e) {
            LOG.error("Scheduler failed with business error: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("Scheduler failed with unexpected error", e);
        }
    }

    // =========================================================================
    // MAIN ENTRY POINT
    // =========================================================================

    public int fetchAndProcessIncomingTransactions() throws Exception {
        LOG.info("=== Starting fetchAndProcessIncomingTransactions ===");

        List<JsonNode> allTransactions = fetchAllTransactionsFromIPS();

        if (allTransactions == null || allTransactions.isEmpty()) {
            LOG.info("No transactions found in IPS output");
            return 0;
        }

        LOG.info("Total transactions from IPS: " + allTransactions.size());

        List<JsonNode> ourTransactions = filterOurTransactions(allTransactions);
        LOG.info("Transactions matching our BIC (" + OUR_BIC + "): " + ourTransactions.size());

        if (ourTransactions.isEmpty()) {
            return 0;
        }

        String accessToken = getWalletCoreAccessToken();

        int processed = 0;
        for (JsonNode tx : ourTransactions) {
            try {
                processIncomingTransaction(tx, accessToken);
                processed++;
            } catch (Exception e) {
                String traceRef = tx.has("traceReference") ? tx.get("traceReference").asText() : "UNKNOWN";
                LOG.error("Failed to process incoming transaction traceReference=" + traceRef, e);
            }
        }

        LOG.info("Successfully processed " + processed + "/" + ourTransactions.size() + " incoming transactions");
        return processed;
    }

    // =========================================================================
    // FETCH FROM IPS
    // =========================================================================

    public List<JsonNode> fetchAllTransactionsFromIPS() throws BusinessException {
        String uuid = UUID.randomUUID().toString();
        String url = ipsOutputUrl + uuid + "?service=ips";

        LOG.info("Calling IPS output URL: " + url);

        WebRequest webRequest = new WebRequest();
        webRequest.setUrl(url);
        webRequest.setQueryMethod(QUERY_METHOD.GET);
        webRequest.setAcceptType("application/json");
        webRequest.setReadTimeout(readTimeout);
        webRequest.setConnectTimeout(connectTimeout);
        webRequest.setHeader(tokenService.buildBearerHeader(tokenService.getAccessToken()));

        try {
            WebInterface.processRequest(webRequest);
        } catch (IOException e) {
            LOG.error("IO error calling IPS output", e);
            throw new BusinessException("Failed to call IPS output endpoint", e);
        }

        int responseCode = webRequest.getResponse().getResponseCode();
        String responseMsg = webRequest.getResponse().getResponseMsg();
        if (responseCode != 200) {
            LOG.error("IPS output returned HTTP " + responseCode + " : " + responseMsg);
        }
        
        
        
        
        
        if (responseCode!= 200) {
        	JSONObject jsonResponse = new JSONObject(responseMsg);
            throw new BusinessException(
            		jsonResponse.optString("description")
            );
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseMsg);
            if (!root.isArray()) {
                LOG.error("IPS output response is not a JSON array");
            }
            List<JsonNode> list = new ArrayList<>();
            root.forEach(list::add);
            return list;
        } catch (Exception e) {
            LOG.error("Failed to parse IPS output JSON", e);
        }
        return null;
    }

    private List<JsonNode> filterOurTransactions(List<JsonNode> allTransactions) {
        List<JsonNode> result = new ArrayList<>();
        for (JsonNode tx : allTransactions) {
            // Filter by receiver
            if (!tx.has("receiver")) continue;
            String receiver = tx.get("receiver").asText("");
            if (!OUR_BIC.equals(receiver) && !receiver.startsWith(OUR_BIC_SHORT)) {
                continue;
            }

            // Filter by type (pacs.008.001.10)
            if (!tx.has("type")) continue;
            String type = tx.get("type").asText("");
            if (!PACS008_TYPE.equals(type)) {
                continue;
            }

            result.add(tx);
        }
        return result;
    }

    // =========================================================================
    // PROCESS ONE TRANSACTION
    // =========================================================================

    private void processIncomingTransaction(JsonNode txNode, String accessToken) throws Exception {
        String traceReference = txNode.has("traceReference") ? txNode.get("traceReference").asText() : "";
        String type           = txNode.has("type")           ? txNode.get("type").asText()           : "";
        String sender         = txNode.has("sender")         ? txNode.get("sender").asText()         : "";
        String documentXml    = txNode.has("document")       ? txNode.get("document").asText()       : "";

        LOG.info("Processing incoming transaction: traceReference=" + traceReference + ", type=" + type);

        if (documentXml.isEmpty()) {
            LOG.warn("Empty document for traceReference=" + traceReference + ", skipping");
            return;
        }

        Map<String, String> txData = extractPacs008Fields(documentXml);
        txData.put("traceReference", traceReference);
        txData.put("messageType", type);
        txData.put("jsonSender", sender); // original sender from JSON wrapper

        LOG.info("Extracted pacs.008 fields for " + traceReference + " : " + txData);

        // Step 1: forward to WalletCore
        if("007".equals(txData.get("purpose"))) {
        	LOG.info("007 sent");
        	sendPacs002Acknowledgment(txData, "AUTH");
        }else {
        	sendToWalletCoreBackend(txData, accessToken);
        }
        
    }
    
    private IpsSendPaymentRequestBo buildPacs002Request(String xmlSenderBic,
            String xmlReceiverBic,
            String senderReference,
            String origRef,
            String origTm,
            String createDate,
            String settlementDate,
            String ackStatus) throws BusinessException {

        // Clean dates
        String createDt = stripTimezone(createDate);
        String origCreDt = stripTimezone(origTm);


        StringBuilder xml = new StringBuilder();


        xml.append("<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">")
           .append("<Body>")

           .append("<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">")

               .append("<Fr>")
                   .append("<FIId>")
                       .append("<FinInstnId>")
                           .append("<BICFI>").append(xmlSenderBic).append("</BICFI>")
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

               .append("<BizMsgIdr>").append(senderReference).append("</BizMsgIdr>")
               .append("<MsgDefIdr>pacs.002.001.12</MsgDefIdr>")
               .append("<BizSvc>brb.ips.01</BizSvc>")
               .append("<CreDt>").append(createDt).append("+02:00</CreDt>")
               .append("<Prty>0100</Prty>")

           .append("</AppHdr>")

           .append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12\">")
           .append("<FIToFIPmtStsRpt>")

               .append("<GrpHdr>")
                   .append("<MsgId>").append(senderReference).append("</MsgId>")
                   .append("<CreDtTm>").append(createDt).append("+02:00</CreDtTm>")
               .append("</GrpHdr>")

               .append("<OrgnlGrpInfAndSts>")
                   .append("<OrgnlMsgId>").append(origRef).append("</OrgnlMsgId>")
                   .append("<OrgnlMsgNmId>pacs.008.001.10</OrgnlMsgNmId>")
                   .append("<OrgnlCreDtTm>").append(origCreDt).append("</OrgnlCreDtTm>")
                   .append("<StsRsnInf><Rsn><Prtry>").append(ackStatus).append("</Prtry></Rsn></StsRsnInf>");
        xml.append("</OrgnlGrpInfAndSts>")

           .append("<TxInfAndSts>")

               .append("<OrgnlInstrId>").append(origRef).append("</OrgnlInstrId>")
               .append("<OrgnlEndToEndId>NOTPROVIDED</OrgnlEndToEndId>")
               .append("<OrgnlTxId>").append(origRef).append("</OrgnlTxId>")

               .append("<InstgAgt>")
                   .append("<FinInstnId>")
                       .append("<BICFI>").append(xmlSenderBic).append("</BICFI>")
                   .append("</FinInstnId>")
               .append("</InstgAgt>")

               .append("<InstdAgt>")
                   .append("<FinInstnId>")
                       .append("<BICFI>").append(xmlReceiverBic).append("</BICFI>")
                   .append("</FinInstnId>")
               .append("</InstdAgt>")

               .append("<OrgnlTxRef>")
                   .append("<IntrBkSttlmDt>").append(settlementDate).append("</IntrBkSttlmDt>")
               .append("</OrgnlTxRef>")

           .append("</TxInfAndSts>")

           .append("</FIToFIPmtStsRpt>")
           .append("</Document>")

           .append("</Body>")
           .append("</DataPDU>");
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
        request.setDocument(signedXml);
        request.setSender("BKGFBIBIAXXX");
        request.setReceiver("BRBUBIBAXIPS");
        request.setTraceReference(senderReference);
        request.setType("pacs.002.001.12");

        return request;
        
    }

/*
    private IpsSendPaymentRequestBo buildPacs002Request(String xmlSenderBic,
            String xmlReceiverBic,
            String senderReference,
            String origRef,
            String origTm,
            String createDate,
            String settlementDate,
            String ackStatus) {

        // Clean dates
        String createDt = stripTimezone(createDate);
        String origCreDt = stripTimezone(origTm);


        StringBuilder xml = new StringBuilder();


        xml.append("<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">")
           .append("<Body>")

           .append("<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">")

               .append("<Fr>")
                   .append("<FIId>")
                       .append("<FinInstnId>")
                           .append("<BICFI>").append(xmlSenderBic).append("</BICFI>")
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

               .append("<BizMsgIdr>").append(senderReference).append("</BizMsgIdr>")
               .append("<MsgDefIdr>pacs.002.001.12</MsgDefIdr>")
               .append("<BizSvc>brb.ips.01</BizSvc>")
               .append("<CreDt>").append(createDt).append("+02:00</CreDt>")
               .append("<Prty>0100</Prty>")

           .append("</AppHdr>")

           .append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12\">")
           .append("<FIToFIPmtStsRpt>")

               .append("<GrpHdr>")
                   .append("<MsgId>").append(senderReference).append("</MsgId>")
                   .append("<CreDtTm>").append(createDt).append("+02:00</CreDtTm>")
               .append("</GrpHdr>")

               .append("<OrgnlGrpInfAndSts>")
                   .append("<OrgnlMsgId>").append(origRef).append("</OrgnlMsgId>")
                   .append("<OrgnlMsgNmId>pacs.008.001.10</OrgnlMsgNmId>")
                   .append("<OrgnlCreDtTm>").append(origCreDt).append("</OrgnlCreDtTm>")
                   .append("<StsRsnInf><Rsn><Prtry>").append(ackStatus).append("</Prtry></Rsn></StsRsnInf>");
        xml.append("</OrgnlGrpInfAndSts>")

           .append("<TxInfAndSts>")

               .append("<OrgnlInstrId>").append(origRef).append("</OrgnlInstrId>")
               .append("<OrgnlEndToEndId>NOTPROVIDED</OrgnlEndToEndId>")
               .append("<OrgnlTxId>").append(origRef).append("</OrgnlTxId>")

               .append("<InstgAgt>")
                   .append("<FinInstnId>")
                       .append("<BICFI>").append(xmlSenderBic).append("</BICFI>")
                   .append("</FinInstnId>")
               .append("</InstgAgt>")

               .append("<InstdAgt>")
                   .append("<FinInstnId>")
                       .append("<BICFI>").append(xmlReceiverBic).append("</BICFI>")
                   .append("</FinInstnId>")
               .append("</InstdAgt>")

               .append("<OrgnlTxRef>")
                   .append("<IntrBkSttlmDt>").append(settlementDate).append("</IntrBkSttlmDt>")
               .append("</OrgnlTxRef>")

           .append("</TxInfAndSts>")

           .append("</FIToFIPmtStsRpt>")
           .append("</Document>")

           .append("</Body>")
           .append("</DataPDU>");
        
        

        IpsSendPaymentRequestBo request = new IpsSendPaymentRequestBo();
        request.setDocument(xml.toString());
        request.setSender("BKGFBIBIAXXX");
        request.setReceiver("BRBUBIBAXIPS");
        request.setTraceReference(senderReference);
        request.setType("pacs.002.001.12");

        return request;
        
    }*/


/*

    private String buildPacs002Xml(
            String xmlSenderBic,
            String xmlReceiverBic,
            String senderReference,
            String origRef,
            String origTm,
            String createDate,
            String settlementDate,
            String ackStatus) {

        // Strip any existing timezone from origTm to re-append +02:00
        String origTmClean = stripTimezone(origTm);
        String createDateClean = stripTimezone(createDate);

        StringBuilder sb = new StringBuilder();
        sb.append("<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">");
        sb.append("<Body>");

        // --- AppHdr ---
        sb.append("<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">");
        sb.append("<Fr><FIId><FinInstnId><BICFI>").append(xmlSenderBic).append("</BICFI></FinInstnId></FIId></Fr>");
        sb.append("<To><FIId><FinInstnId><BICFI>").append(xmlReceiverBic).append("</BICFI></FinInstnId></FIId></To>");
        sb.append("<BizMsgIdr>").append(senderReference).append("</BizMsgIdr>");
        sb.append("<MsgDefIdr>pacs.002.001.12</MsgDefIdr>");
        sb.append("<BizSvc>brb.ips.01</BizSvc>");
        sb.append("<CreDt>").append(createDateClean).append("+02:00</CreDt>");
        sb.append("<Prty>0100</Prty>");
        sb.append("</AppHdr>");

        // --- Document ---
        sb.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12\">");
        sb.append("<FIToFIPmtStsRpt>");

        // GrpHdr
        sb.append("<GrpHdr>");
        sb.append("<MsgId>").append(senderReference).append("</MsgId>");
        sb.append("<CreDtTm>").append(createDateClean).append("+02:00</CreDtTm>");
        sb.append("</GrpHdr>");

        // OrgnlGrpInfAndSts
        sb.append("<OrgnlGrpInfAndSts>");
        sb.append("<OrgnlMsgId>").append(origRef).append("</OrgnlMsgId>");
        sb.append("<OrgnlMsgNmId>pacs.008.001.10</OrgnlMsgNmId>");
        sb.append("<OrgnlCreDtTm>").append(origTmClean).append("+02:00</OrgnlCreDtTm>");
        sb.append("<StsRsnInf><Rsn><Prtry>").append(ackStatus).append("</Prtry></Rsn></StsRsnInf>");
        sb.append("</OrgnlGrpInfAndSts>");

        // TxInfAndSts
        sb.append("<TxInfAndSts>");
        sb.append("<OrgnlInstrId>").append(origRef).append("</OrgnlInstrId>");
        sb.append("<OrgnlEndToEndId>NOTPROVIDED</OrgnlEndToEndId>");
        sb.append("<OrgnlTxId>").append(origRef).append("</OrgnlTxId>");
        sb.append("<InstgAgt><FinInstnId><BICFI>").append(xmlSenderBic).append("</BICFI></FinInstnId></InstgAgt>");
        sb.append("<InstdAgt><FinInstnId><BICFI>").append(xmlReceiverBic).append("</BICFI></FinInstnId></InstdAgt>");
        sb.append("<OrgnlTxRef><IntrBkSttlmDt>").append(settlementDate).append("</IntrBkSttlmDt></OrgnlTxRef>");
        sb.append("</TxInfAndSts>");

        sb.append("</FIToFIPmtStsRpt>");
        sb.append("</Document>");
        sb.append("</Body>");
        sb.append("</DataPDU>");

        return sb.toString();
    }
*/

    private String currentIso8601() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
    }

    private String stripTimezone(String datetime) {
        if (datetime == null || datetime.isEmpty()) return "";
        // Remove +HH:MM or -HH:MM suffix
        return datetime.trim().replaceAll("[+-]\\d{2}:\\d{2}$", "");
    }


    private String generateMessageId() {
        String ts = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int rand  = (int)(Math.random() * 99999);
        return "T" + ts + String.format("%05d", rand);
    }

    // =========================================================================
    // XML PARSING
    // =========================================================================

    public static Map<String, String> extractPacs008Fields(String documentXml) throws Exception {
        Map<String, String> result = new HashMap<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(documentXml)));

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();

        xpath.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                switch (prefix) {
                    case "p": return "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.10";
                    case "h": return "urn:iso:std:iso:20022:tech:xsd:head.001.001.02";
                    case "s": return "urn:cma:stp:xsd:stp.1.0";
                    default:  return XMLConstants.NULL_NS_URI;
                }
            }
            @Override public String getPrefix(String uri) { return null; }
            @Override public java.util.Iterator getPrefixes(String uri) { return null; }
        });

        result.put("bizMsgIdr",       safeXpath(xpath, "//h:AppHdr/h:BizMsgIdr", doc));
        result.put("fromBic",         safeXpath(xpath, "//h:AppHdr/h:Fr/h:FIId/h:FinInstnId/h:BICFI", doc));
        result.put("toBic",           safeXpath(xpath, "//h:AppHdr/h:To/h:FIId/h:FinInstnId/h:BICFI", doc));
        result.put("creationDate",    safeXpath(xpath, "//h:AppHdr/h:CreDt", doc));
        result.put("msgId",           safeXpath(xpath, "//p:GrpHdr/p:MsgId", doc));
        result.put("creDtTm",         safeXpath(xpath, "//p:GrpHdr/p:CreDtTm", doc));
        result.put("instrId",         safeXpath(xpath, "//p:CdtTrfTxInf/p:PmtId/p:InstrId", doc));
        result.put("endToEndId",      safeXpath(xpath, "//p:CdtTrfTxInf/p:PmtId/p:EndToEndId", doc));
        result.put("txId",            safeXpath(xpath, "//p:CdtTrfTxInf/p:PmtId/p:TxId", doc));
        result.put("uetr",            safeXpath(xpath, "//p:CdtTrfTxInf/p:PmtId/p:UETR", doc));
        result.put("clearingChannel", safeXpath(xpath, "//p:CdtTrfTxInf/p:PmtTpInf/p:ClrChanl", doc));
        result.put("localInstrument", safeXpath(xpath, "//p:CdtTrfTxInf/p:PmtTpInf/p:LclInstrm/p:Prtry", doc));
        result.put("amount",          safeXpath(xpath, "//p:CdtTrfTxInf/p:IntrBkSttlmAmt", doc));
        result.put("currency",        safeXpathAttr(xpath, "//p:CdtTrfTxInf/p:IntrBkSttlmAmt/@Ccy", doc));
        result.put("settlementDate",  safeXpath(xpath, "//p:CdtTrfTxInf/p:IntrBkSttlmDt", doc));
        result.put("instgAgt",        getFinInstId(xpath, "//p:CdtTrfTxInf/p:InstgAgt/p:FinInstnId", doc));
        result.put("instdAgt",        getFinInstId(xpath, "//p:CdtTrfTxInf/p:InstdAgt/p:FinInstnId", doc));
        result.put("dbtrAgt",         getFinInstId(xpath, "//p:CdtTrfTxInf/p:DbtrAgt/p:FinInstnId", doc));
        result.put("cdtrAgt",         getFinInstId(xpath, "//p:CdtTrfTxInf/p:CdtrAgt/p:FinInstnId", doc));
        result.put("debtorName",      safeXpath(xpath, "//p:CdtTrfTxInf/p:Dbtr/p:Nm", doc));
        result.put("debtorAccount",   safeXpath(xpath, "//p:CdtTrfTxInf/p:DbtrAcct/p:Id/p:Othr/p:Id", doc));
        result.put("creditorName",    safeXpath(xpath, "//p:CdtTrfTxInf/p:Cdtr/p:Nm", doc));
        result.put("creditorAccount", safeXpath(xpath, "//p:CdtTrfTxInf/p:CdtrAcct/p:Id/p:Othr/p:Id", doc));
        result.put("purpose",         safeXpath(xpath, "//p:CdtTrfTxInf/p:Purp/p:Prtry", doc));
        result.put("remittanceInfo",  safeXpath(xpath, "//p:CdtTrfTxInf/p:RmtInf/p:Ustrd", doc));

        return result;
    }

    private static String getFinInstId(XPath xpath, String basePath, Document doc) {
        String bicfi = safeXpath(xpath, basePath + "/p:BICFI", doc);
        if (bicfi != null && !bicfi.isEmpty()) return bicfi;
        return safeXpath(xpath, basePath + "/p:ClrSysMmbId/p:MmbId", doc);
    }

    private static String safeXpath(XPath xpath, String expression, Document doc) {
        try {
            String value = xpath.evaluate(expression, doc);
            return value != null ? value.trim() : "";
        } catch (Exception e) { return ""; }
    }

    private static String safeXpathAttr(XPath xpath, String expression, Document doc) {
        return safeXpath(xpath, expression, doc);
    }

    // =========================================================================
    // PAYMENT BUILDER
    // =========================================================================

    private Payment buildPaymentFromIsoData(Map<String, String> txData) throws BusinessException {
        Payment payment = new Payment();

        payment.setAmount(parseAmount(txData.get("amount")));
        payment.setCurrency("108");
        payment.setAcquirerTrxRef(txData.get("traceReference"));
        payment.setDescription(txData.getOrDefault("remittanceInfo", "IPS Incoming Transfer"));
        payment.setCreateTime(new Date());

        String purpose = txData.get("purpose");

        if ("001".equals(purpose)) {
            List<AccountInfo> srcAccounts = new ArrayList<>();
            List<AccountInfo> dstAccounts = new ArrayList<>();
            AccountInfo accountSrc = new AccountInfo();
            AccountInfo accountDst = new AccountInfo();
            
            accountSrc.setIden(txData.get("debtorAccount"));
            accountSrc.setName(txData.get("debtorName"));
            accountSrc.setType("ACCOUNT");
            
            accountDst.setIden(txData.get("creditorAccount"));
            accountDst.setName(txData.get("creditorName"));
            accountDst.setType("ACCOUNT");
            srcAccounts.add(accountSrc);
            dstAccounts.add(accountDst);
            
            payment.setSrcAccounts(srcAccounts);
            payment.setDstAccounts(dstAccounts);
            payment.setIntent("inst_account_transf");

        } else if ("002".equals(purpose) ) {
        	List<AccountInfo> srcAccounts = new ArrayList<>();
        	AccountInfo accountSrc = new AccountInfo();
        	accountSrc.setIden(txData.get("debtorAccount"));
            accountSrc.setName(txData.get("debtorName"));
            srcAccounts.add(accountSrc);
            payment.setSrcAccounts(srcAccounts);
            payment.setWalletSource(txData.get("debtorAccount"));
            payment.setWalletDestination(txData.get("creditorAccount"));
            payment.setIntent("inst_mobile_transfer");

        } 
        else if("003".equals(purpose)) {
        	List<AccountInfo> dstAccounts = new ArrayList<>();
        	AccountInfo accountDst = new AccountInfo();
        	accountDst.setIden(txData.get("creditorAccount"));
        	accountDst.setType("ACCOUNT");
        	accountDst.setName(txData.get("creditorName"));
        	dstAccounts.add(accountDst);
            payment.setDstAccounts(dstAccounts);
            payment.setWalletSource(txData.get("debtorAccount"));
            payment.setIntent("inst_wallet_to_acc");
        }
        else if("007".equals(purpose)) {
        	//TODO ADD this cas
        }
        else {
            LOG.info("Unknown purpose code: " + purpose + " - defaulting to wallet transfer.");
            throw new BusinessException("Unknown purpose code "+purpose);
        }

        payment.setVoucherCode(CoreUtil.generateVoucher("AN", 9));
        payment.setSendSourceHandler("BRB_IPS");
        payment.setState("ACCEPTED");
        payment.setToMember("20005");
        payment.setFromMember("BRBUBIBAIPS");

        LOG.debug("Built WalletPaymentBo from IPS data: " + payment.toString());
        return payment;
    }

    private Double parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            LOG.warn("Amount is null or empty, defaulting to 0.0");
            return 0.0;
        }
        try {
            return Double.parseDouble(amountStr.trim());
        } catch (NumberFormatException e) {
            LOG.error("Cannot parse amount: '" + amountStr + "', defaulting to 0.0");
            return 0.0;
        }
    }

    // =========================================================================
    // WALLET CORE CALL
    // =========================================================================

    private void sendToWalletCoreBackend(Map<String, String> txData, String accessToken) {

        Payment paymentResponse = null;   // stays null on failure
        String  ackStatus       = "NAUT"; // safe default: rejected

        try {
        	Payment payment = buildPaymentFromIsoData(txData);

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + accessToken);

            WebRequest req = new WebRequest();
            req.setAcceptType(acceptType);
            req.setMediaType(mediaTypeJson);
            req.setHeader(headers);
            req.setReadTimeout(readTimeout);
            req.setConnectTimeout(connectTimeout);
            req.setUrl(urlIncomingCreate);
            req.setQueryMethod(QUERY_METHOD.POST);
            req.setBody(payment);

            WebInterface.processRequest(req);

            int    code    = req.getResponse().getResponseCode();
            String respMsg = req.getResponse().getResponseMsg();

            if (code != 200 && code != 201) {
                LOG.error("WalletCore rejected incoming IPS transaction."
                        + " HTTP="      + code
                        + ", traceRef=" + txData.get("traceReference")
                        + ", response=" + respMsg);
                ackStatus="NAUT";
            } else {
                // Happy path: parse the response and derive the real ack status
                ObjectMapper objectMapper = new ObjectMapper();
                paymentResponse = objectMapper.readValue(respMsg, Payment.class);
                ackStatus       = resolveAckStatus(paymentResponse);

                LOG.info("Incoming IPS transaction successfully sent to WalletCore:"
                        + " traceRef=" + txData.get("traceReference")
                        + ", amount="  + txData.get("amount") + " " + txData.get("currency")
                        + ", state="   + paymentResponse.getState());
            }

        } catch (Exception e) {
            LOG.error("Exception while sending transaction to WalletCore."
                    + " traceRef=" + txData.getOrDefault("traceReference", "UNKNOWN"), e);
            ackStatus="NAUT";
        } finally {
            sendPacs002Acknowledgment(txData, ackStatus);
        }
    }


    private String resolveAckStatus(Payment paymentResponse) {
        if (paymentResponse == null || paymentResponse.getState() == null) {
            return "NAUT";
        }
        switch (paymentResponse.getState().toUpperCase()) {
            case "ACCEPTED": return "AUTH";
            case "SUSPECTED":  return "SUSP";
            default:         return "NAUT";
        }
    }


    public void sendPacs002Acknowledgment(Map<String, String> txData, String ackStatus) {
        try {
            // Retrieve required data from txData
            String traceReference = txData.getOrDefault("traceReference", "");
            String senderReference = generateMessageId();
            String origRef = txData.getOrDefault("msgId", traceReference);
            String origTm = txData.getOrDefault("creDtTm", "");
            String createDate = currentIso8601();
            String settlementDate = txData.getOrDefault("settlementDate", createDate.substring(0, 10));
            String xmlSenderBic = "BKGFBIBI";
            String xmlReceiverBic = txData.getOrDefault("instgAgt", "");;

            if (ackStatus == null) {
                ackStatus = "NAUT";
            }

            // Build the request object for pacs.002
            IpsSendPaymentRequestBo request = buildPacs002Request(
                    xmlSenderBic,
                    xmlReceiverBic,
                    senderReference,
                    origRef,
                    origTm,
                    createDate,
                    settlementDate,
                    ackStatus
            );

            
            // Send the request to the IPS API
            String uuid = UUID.randomUUID().toString();
            String url = ipsInputUrl + uuid + "?service=ips";

            WebRequest req = new WebRequest();
            req.setUrl(url);
            req.setQueryMethod(QUERY_METHOD.POST);
            req.setAcceptType("application/json");
            req.setMediaType("application/json");
            req.setReadTimeout(readTimeout);
            req.setConnectTimeout(connectTimeout);
            req.setHeader(tokenService.buildBearerHeader(tokenService.getAccessToken()));
            req.setBody(request);

            WebInterface.processRequest(req);

            int code = req.getResponse().getResponseCode();
            String respBody = req.getResponse().getResponseMsg();

            if (code == 200 || code == 201 || code == 202) {
                LOG.info("pacs.002 acknowledgment accepted by IPS. HTTP=" + code + ", traceRef=" + traceReference);
            } else {
                LOG.error("IPS rejected pacs.002 acknowledgment. HTTP=" + code + ", response=" + respBody);
            }

        } catch (Exception e) {
            LOG.error("Failed to send pacs.002 acknowledgment for traceRef=" + txData.getOrDefault("traceReference", "UNKNOWN"), e);
        }
    }




    // =========================================================================
    // TOKEN
    // =========================================================================

    private String getWalletCoreAccessToken() throws Exception {
        WebRequest tokenRequest = buildTokenRequest();
        WebInterface.processRequest(tokenRequest);
        if (tokenRequest.getResponse().getResponseCode() != 200) {
            throw new BusinessException("Error getting WalletCore access token");
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode tokenNode = mapper.readTree(tokenRequest.getResponse().getResponseMsg());
        return tokenNode.get("access_token").asText();
    }

    private WebRequest buildTokenRequest() {
        WebRequest req = new WebRequest();
        req.setAcceptType(acceptType);
        req.setMediaType(mediaTypeForm);
        req.setReadTimeout(readTimeout);
        req.setConnectTimeout(connectTimeout);
        req.setUrl(urlToken);
        req.setQueryMethod(QUERY_METHOD.POST);
        req.setBody("grant_type=password&client_id=restapp&client_secret=restapp&scope=read&username=ips&password=payway100");
        return req;
    }

    // =========================================================================
    // REST TRIGGER
    // =========================================================================

    public ResponseEntity<String> pollAndProcessIncoming() {
        try {
            int count = fetchAndProcessIncomingTransactions();
            return ResponseEntity.ok("Processed " + count + " incoming transactions");
        } catch (BusinessException e) {
            LOG.error("Business error during incoming poll", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Unexpected error during incoming poll", e);
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }
}