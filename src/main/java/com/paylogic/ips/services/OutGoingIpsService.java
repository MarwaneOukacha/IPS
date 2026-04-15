package com.paylogic.ips.services;

import java.io.IOException;
import java.io.StringReader;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gms.utils.common.StringUtil;
import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.WebInterface;
import com.gms.utils.net.webinterface.WebRequest;
import com.gms.utils.net.webinterface.WebRequest.QUERY_METHOD;
import com.paylogic.ama.core.model.AccountInfo;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ama.core.utils.BaseCoreUtil;
import com.paylogic.ama.core.utils.BaseCoreUtil.PAYMENT_ACTION;
import com.paylogic.ama.wm.core.bo.EntityPayment;
import com.paylogic.ips.bo.IpsSendPaymentRequestBo;
import com.paylogic.ips.converter.incoming.Pacs002IncomingConverter;
import com.paylogic.ips.converter.outgoing.Pacs008OutgoingConverter;
import com.paylogic.ips.iso20022.bo.BaseDocument;
import com.paylogic.ips.iso20022.bo.pacs002.DocumentPacs002;
import com.paylogic.ips.iso20022.bo.pacs002.FIToFIPaymentStatusReportV13;
import com.paylogic.ips.iso20022.bo.pacs002.PaymentTransaction142;
import com.paylogic.ips.util.CoreUtil;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.StringReader;
import org.xml.sax.InputSource;

@Service
public class OutGoingIpsService {

    private static final Logger LOG = Logger.getLogger(OutGoingIpsService.class);

    @Autowired
    private Pacs008OutgoingConverter pacs008Converter;
    
    @Autowired private Pacs002IncomingConverter pacs002IncomingConverter;

    @Autowired
    private CasService casService;

    @Autowired
    private TokenService tokenService;

    @Value("${user}")
    private String user;

    @Value("${receiver}")
    private String receiver;

    @Value("${pacs008DebetorUrl}")
    private String pacs008DebetorUrl;

    @Value("${casResolveUrl}")
    private String casResolveUrl;
    @Value("${checkOutpayment}")
    private String checkOutpayment;
    
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
    @Value("${walletcore.url.update}")
    private String urlupdate;
    /**
     * Main entry point to send a payment to IPS
     */
    public Payment sendPaymentToIPS(EntityPayment payment) throws BusinessException, IOException {

        validatePayment(payment);

        IpsSendPaymentRequestBo request = buildIpsRequest(payment);

        String traceRef = payment.getPayment().getIssuerTrxRef();
        LOG.info("Sending payment to IPS, traceRef=" + traceRef);

        WebRequest webRequest = new WebRequest();
        webRequest.setUrl(pacs008DebetorUrl + traceRef + "?service=ips");
        webRequest.setQueryMethod(WebRequest.QUERY_METHOD.POST);
        webRequest.setAcceptType("application/json");
        webRequest.setMediaType("application/json");
        webRequest.setHeader(tokenService.buildBearerHeader(tokenService.getAccessToken()));
        webRequest.setBody(request);

        try {
            WebInterface.processRequest(webRequest);
        } catch (IOException e) {
            LOG.error("Sending to IPS failed", e);
            throw new BusinessException("Failed to send payment to IPS", e);
        }

        if (webRequest.getResponse().getResponseCode() != 200) {
            LOG.error("Sending to IPS failed: " + webRequest.getResponse().getResponseMsg());
            throw new BusinessException("Failed to send payment to IPS");
        }
        //payment.getPayment().setState(CoreUtil.PAY_STATUS_PENDING);
        return payment.getPayment();
    }

    /**
     * Build IPS request payload using Payment object
     */
    private IpsSendPaymentRequestBo buildIpsRequest(EntityPayment entitypayment) throws BusinessException {
    	Payment payment=entitypayment.getPayment();
        // Get current date/time
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("+02:00"));
        String creationDateTime = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String dateOnly = now.toLocalDate().toString();

        // Format amount according to ISO 4217
        String amountFormatted;
        if ("108".equals(payment.getCurrency())) {
            amountFormatted = String.valueOf(payment.getAmount().longValue()); // no decimals
        } else {
            amountFormatted = payment.getAmount().toString();
        }

        // Ensure EndToEndId is max 35 chars
        String endToEndId = "xx";
        if (payment != null && payment.getDescription() != null) {
            endToEndId = payment.getDescription();
        }
        if (endToEndId.length() > 35) {
            endToEndId = endToEndId.substring(0, 35);
        }
        String currencyAlpha = getCurrencyAlpha(payment.getCurrency());
        String ttc=null;
        AccountInfo dstAccount = payment.getDstAccounts().get(0);
        if(dstAccount.getType().equals("WALLET")) {
        	ttc="002";
        }else  {
        	ttc="001";
        }
        String bic=dstAccount.getVerificationData().get("title");
        // Build XML safely
        StringBuilder xml = new StringBuilder();
        xml.append("<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">")
           .append("<Body>")
               .append("<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">")
                   .append("<Fr><FIId><FinInstnId><BICFI>BKGFBIBI</BICFI></FinInstnId></FIId></Fr>")
                   .append("<To><FIId><FinInstnId><BICFI>"+bic+"</BICFI></FinInstnId></FIId></To>")
                   .append("<BizMsgIdr>").append(payment.getIssuerTrxRef()).append("</BizMsgIdr>")
                   .append("<MsgDefIdr>pacs.008.001.10</MsgDefIdr>")
                   .append("<BizSvc>brb.ips.01</BizSvc>")
                   .append("<CreDt>").append(creationDateTime).append("</CreDt>")
                   .append("<Prty>0100</Prty>")
               .append("</AppHdr>")
               .append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.10\">")
                   .append("<FIToFICstmrCdtTrf>")
                       .append("<GrpHdr>")
                           .append("<MsgId>").append(payment.getIssuerTrxRef()).append("</MsgId>")
                           .append("<CreDtTm>").append(creationDateTime).append("</CreDtTm>")
                           .append("<NbOfTxs>1</NbOfTxs>")
                           .append("<SttlmInf><SttlmMtd>CLRG</SttlmMtd></SttlmInf>")
                       .append("</GrpHdr>")
                       .append("<CdtTrfTxInf>")
                           .append("<PmtId>")
                               .append("<InstrId>").append(payment.getIssuerTrxRef()).append("</InstrId>")
                               .append("<EndToEndId>").append(endToEndId).append("</EndToEndId>")
                               .append("<TxId>").append(payment.getIssuerTrxRef()).append("</TxId>")
                           .append("</PmtId>")
                           .append("<PmtTpInf>")
                               .append("<ClrChanl>RTNS</ClrChanl>")
                               .append("<LclInstrm><Prtry>TRFI</Prtry></LclInstrm>")
                           .append("</PmtTpInf>")
                           .append("<IntrBkSttlmAmt Ccy=\"").append(currencyAlpha).append("\">")
                               .append(amountFormatted)
                           .append("</IntrBkSttlmAmt>")
                           .append("<IntrBkSttlmDt>").append(dateOnly).append("</IntrBkSttlmDt>")
                           .append("<ChrgBr>SLEV</ChrgBr>")
                           .append("<InstgAgt><FinInstnId><BICFI>BKGFBIBI</BICFI></FinInstnId></InstgAgt>")
                           .append("<InstdAgt><FinInstnId><BICFI>"+bic+"</BICFI></FinInstnId></InstdAgt>")
                           .append("<Dbtr><Nm>").append(payment.getSenderMobile()).append("</Nm></Dbtr>")
                           .append("<DbtrAcct><Id><Othr><Id>").append(payment.getAccountNumber()).append("</Id></Othr></Id></DbtrAcct>")
                           .append("<DbtrAgt><FinInstnId><BICFI>BKGFBIBI</BICFI></FinInstnId></DbtrAgt>")
                           .append("<CdtrAgt><FinInstnId><BICFI>"+bic+"</BICFI></FinInstnId></CdtrAgt>")
                           .append("<Cdtr><Nm>").append(payment.getReceiverCustomerData().getSurname()).append("</Nm></Cdtr>")
                           .append("<CdtrAcct><Id><Othr><Id>").append(payment.getReceiverCustomerData().getSurname()).append("</Id></Othr></Id></CdtrAcct>")
                           .append("<Purp><Prtry>"+ttc+"</Prtry></Purp>")
                           .append("<RmtInf><Ustrd>").append(payment.getDescription()).append("</Ustrd></RmtInf>")
                       .append("</CdtTrfTxInf>")
                   .append("</FIToFICstmrCdtTrf>")
               .append("</Document>")
           .append("</Body>")
        .append("</DataPDU>");

        // Build request object
        IpsSendPaymentRequestBo request = new IpsSendPaymentRequestBo();
        request.setDocument(xml.toString());
        request.setSender(user);
        request.setReceiver(receiver);
        request.setTraceReference(payment.getIssuerTrxRef());
        request.setType("pacs.008.001.10");

        return request;
    }



    private void validatePayment(EntityPayment payment) throws BusinessException {
        if (payment == null) throw new BusinessException("Payment cannot be null");
        if (StringUtil.isNullOrEmpty(payment.getPayment().getIntent())) throw new BusinessException("Missing payment intent");
        if (payment.getAction() == null) throw new BusinessException("Missing payment action");
    }

    public BaseDocument getOutgoingDocument(Payment payment) throws BusinessException {
        PAYMENT_ACTION action = payment.getAction();
        String intent = payment.getIntent();

        LOG.info("Generating outgoing document, intent=" + intent + ", action=" + action);

        switch (intent) {
            case CoreUtil.WALLET_TRANSFER:
            case CoreUtil.WALLET_TO_ACCOUNT_TRANSFER:
            case CoreUtil.BANK_ACCOUNT_TRANSFER:
                if (PAYMENT_ACTION.SEND.equals(action)) {
                    return pacs008Converter.convertPayment(payment, null);
                }
                break;
            default:
                LOG.error("Unsupported outgoing intent: " + intent);
                throw new BusinessException("Unsupported outgoing intent: " + intent);
        }
        throw new BusinessException("Unsupported action " + action + " for intent " + intent);
    }

    public ResponseEntity<String> checkPaymentStatusInIPS(String traceReference) throws BusinessException {
        if (StringUtil.isNullOrEmpty(traceReference)) {
            return ResponseEntity.badRequest().body("Missing traceReference");
        }

        try {
            // Construire la requête vers IPS pour vérifier le statut du paiement
            String statusUrl = checkOutpayment + traceReference + "?service=ips";
            
            WebRequest webRequest = new WebRequest();
            webRequest.setUrl(statusUrl);
            webRequest.setQueryMethod(WebRequest.QUERY_METHOD.GET);
            webRequest.setAcceptType("application/json");
            webRequest.setHeader(tokenService.buildBearerHeader(tokenService.getAccessToken()));

            WebInterface.processRequest(webRequest);

            int code = webRequest.getResponse().getResponseCode();
            String responseBody = webRequest.getResponse().getResponseMsg();

            if (code != 200) {
                LOG.error("Failed to check payment status in IPS. HTTP Code: " + code + ", Response: " + responseBody);
                return ResponseEntity.status(code).body("Error checking status: " + responseBody);
            }

            
            LOG.info("Payment status response from IPS: " + responseBody);

            return ResponseEntity.ok(responseBody);

        } catch (IOException e) {
            LOG.error("Error checking payment status in IPS", e);
            return ResponseEntity.status(500).body("Internal error while checking payment status");
        }
    }

    public ResponseEntity<String> receivePacs002(String pacs002Xml) {
        try {
            // 1. Convert XML → DocumentPacs002
            DocumentPacs002 doc = convertToPaymentResponse(pacs002Xml);

            FIToFIPaymentStatusReportV13 report = doc.getFIToFIPmtStsRpt();

            if (report.getTxInfAndSts() == null || report.getTxInfAndSts().isEmpty()) {
                LOG.error("PACS.002 received but no TxInfAndSts found");
                return ResponseEntity.badRequest().body("Invalid pacs.002 format");
            }

            PaymentTransaction142 tx = report.getTxInfAndSts().get(0);

            // 2. Extract EndToEndId, Status, Reason
            String endToEndId = tx.getOrgnlEndToEndId();
            String txId = tx.getOrgnlTxId();
            String status = tx.getTxSts();            // ACSP / RJCT
            String reason = null;

            if (tx.getStsRsnInf() != null && !tx.getStsRsnInf().isEmpty()) {
                reason = tx.getStsRsnInf().get(0).getRsn().getCd(); // Optionnel
            }

            LOG.info(" Received PACS.002 -> EndToEndId=" + endToEndId 
                    + ", TxId=" + txId 
                    + ", Status=" + status 
                    + ", Reason=" + reason);

            // 3. Update Payment in WalletCore or DB
            WebRequest tokenRequest = buildTokenRequest();
            WebInterface.processRequest(tokenRequest);

           
            if (tokenRequest.getResponse().getResponseCode() != 200) {
                LOG.error("WalletCoreAuthentication::error getting access token :: " + tokenRequest.getResponse());
                throw new BusinessException("error getting access token: " + tokenRequest.getResponse());
            }
            String tokenJson = tokenRequest.getResponse().getResponseMsg();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(tokenJson);

            String accessToken = rootNode.get("access_token").asText();

            LOG.info("Access Token: " + accessToken);
         // 3. Build request body as Map
            Map<String, String> bodyMap = new HashMap<>();
            bodyMap.put("traceReference", txId);
            if(status.equals("ACSP")) {
            	bodyMap.put("status", CoreUtil.PAY_STATUS_ACCEPTED);
            }else {
            	bodyMap.put("status", CoreUtil.PAY_STATUS_REJECTED);
            }
            

            WebRequest req = new WebRequest();
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + accessToken);
            req.setAcceptType(acceptType);
            req.setMediaType(mediaTypeJson);
            req.setHeader(headers);
            req.setReadTimeout(readTimeout);
            req.setConnectTimeout(connectTimeout);
            req.setUrl(urlupdate);
            req.setQueryMethod(QUERY_METHOD.POST);
            req.setBody(bodyMap);
            
            WebInterface.processRequest(req);
            
            return ResponseEntity.ok("Received");

        } catch (Exception e) {
            LOG.error(" Failed to process PACS.002", e);
            return ResponseEntity.status(500).body("Error processing pacs.002");
        }
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

	
	private DocumentPacs002 convertToPaymentResponse(String pacs002Xml) throws Exception {
	    try {
	        JAXBContext jaxbContext = JAXBContext.newInstance(DocumentPacs002.class);
	        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

	        // Important pour gérer les namespaces ISO20022
	        StringReader reader = new StringReader(pacs002Xml);
	        JAXBElement<DocumentPacs002> root = unmarshaller.unmarshal(
	                new StreamSource(reader), 
	                DocumentPacs002.class
	        );

	        return root.getValue();

	    } catch (JAXBException e) {
	        LOG.error(" Failed to unmarshal PACS.002 XML", e);
	        throw new Exception("Unable to parse PACS.002 XML", e);
	    }
	}

	private String getCurrencyAlpha(String currency) {
	    switch(currency) {
	        case "108": return "BIF";
	        
	        default: return currency; // assume already alpha code
	    }
	}
	public static Map<String, String> extractStatusAndReason(String xml) throws Exception {

	    Map<String, String> result = new HashMap<>();

	    // Parse XML
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(true);
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document doc = db.parse(new InputSource(new StringReader(xml)));

	    XPathFactory xpf = XPathFactory.newInstance();
	    XPath xpath = xpf.newXPath();

	    // Namespace
	    xpath.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {
	        @Override
	        public String getNamespaceURI(String prefix) {
	            if ("d".equals(prefix)) {
	                return "urn:swift:xsd:pacs.002.001.12";
	            }
	            return XMLConstants.NULL_NS_URI;
	        }
	        @Override public String getPrefix(String uri) { return null; }
	        @Override public java.util.Iterator getPrefixes(String uri) { return null; }
	    });

	    //  Status
	    String status = xpath.evaluate("//d:TxInfAndSts/d:TxSts", doc);
	    result.put("status", status);

	    //  Reason Code (ER2)
	    String reasonCode = xpath.evaluate(
	        "//d:TxInfAndSts/d:StsRsnInf/d:Rsn/d:Prtry",
	        doc
	    );

	    //  Reason Message (first AddtlInf)
	    String reasonMessage = xpath.evaluate(
	        "(//d:TxInfAndSts/d:StsRsnInf/d:AddtlInf)[1]",
	        doc
	    );

	    result.put("reasonCode", reasonCode);
	    result.put("reasonMessage", reasonMessage);

	    return result;
	}
	public Boolean processStatusFromIPS(String traceReference) throws Exception {

	    ResponseEntity<String> response = checkPaymentStatusInIPS(traceReference);

	    if (!response.getStatusCode().is2xxSuccessful()) {
	        LOG.error("Failed to call IPS");
	        throw new BusinessException("Failed to call IPS");
	    }

	    String json = response.getBody();

	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode array = mapper.readTree(json);

	    if (!array.isArray()) {
	        LOG.error("Invalid IPS response: not an array");
	        throw new BusinessException("Invalid IPS response");
	    }

	    JsonNode matchedTx = null;

	    // FIND YOUR TRANSACTION
	    for (JsonNode tx : array) {
	        String txRef = tx.get("traceReference").asText();
	        if (traceReference.equals(txRef)) {
	            matchedTx = tx;
	            break;
	        }
	    }

	    if (matchedTx == null) {
	        LOG.warn("Transaction not found: " + traceReference);
	        throw new BusinessException("Transaction not found");
	    }

	    // EXTRACT XML
	    String pacs002Xml = matchedTx.get("document").asText();

	    try {
	        // Extract status directly
	         Map<String, String> resultt = extractStatusAndReason(pacs002Xml);
	         String status=resultt.get("status");
	         String reasonMessage=resultt.get("reasonMessage");
	         String reasonCode=resultt.get("reasonCode");
	         
	        LOG.info("PACS.002 extracted: " +resultt );

	        // Prepare backend call
	        WebRequest tokenRequest = buildTokenRequest();
	        WebInterface.processRequest(tokenRequest);

	        if (tokenRequest.getResponse().getResponseCode() != 200) {
	            throw new BusinessException("Error getting token");
	        }

	        JsonNode tokenNode = mapper.readTree(tokenRequest.getResponse().getResponseMsg());
	        String accessToken = tokenNode.get("access_token").asText();

	        // Prepare request body
	        //Map<String, String> bodyMap = new HashMap<>();
	        resultt.put("traceReference", traceReference);
	        if ("ACSP".equals(status)) {
	        	resultt.put("status", CoreUtil.PAY_STATUS_ACCEPTED);
	        } else {
	        	resultt.put("status", CoreUtil.PAY_STATUS_REJECTED);
	        }
	        resultt.put("reasonMessage", reasonMessage);
	        resultt.put("reasonCode", reasonCode);
	        LOG.info("body map  " + resultt);

	        // Call backend
	        WebRequest req = new WebRequest();
	        Map<String, String> headers = new HashMap<>();
	        headers.put("Authorization", "Bearer " + accessToken);

	        req.setAcceptType(acceptType);
	        req.setMediaType(mediaTypeJson);
	        req.setHeader(headers);
	        req.setReadTimeout(readTimeout);
	        req.setConnectTimeout(connectTimeout);
	        req.setUrl(urlupdate);
	        req.setQueryMethod(QUERY_METHOD.POST);
	        req.setBody(resultt);

	        WebInterface.processRequest(req);

	        LOG.info("Backend updated successfully for " + traceReference);

	    } catch (Exception e) {
	        LOG.error("Failed to process PACS.002", e);
	    }
	    return true;
	}
}
