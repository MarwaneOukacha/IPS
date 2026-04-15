package com.paylogic.ips.services;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.WebInterface;
import com.gms.utils.net.webinterface.WebRequest;
import com.gms.utils.net.webinterface.WebRequest.QUERY_METHOD;
import com.paylogic.ama.core.utils.BaseCoreUtil;
import com.paylogic.ama.wm.core.bo.WalletPaymentBo;
import com.paylogic.ips.util.CoreUtil;

@Service
public class IncomingIpsService {

    private static final Logger LOG = Logger.getLogger(IncomingIpsService.class);

    private static final String OUR_BIC = "BKGFBIBIXXXX";

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

    @Value("${walletcore.url.incoming.create}")
    private String urlIncomingCreate;

    @Value("${ips.incoming.scheduler.enabled}")
    private boolean schedulerEnabled;

    // =========================================================================
    // SCHEDULER - Exécuté automatiquement toutes les 5 minutes
    // =========================================================================

    /**
     * Scheduled task qui s'exécute toutes les 5 minutes pour récupérer
     * et traiter automatiquement les transactions entrantes depuis IPS.
     * 
     * Peut être activé/désactivé via la propriété:
     *   ips.incoming.scheduler.enabled=true/false
     */
    @Scheduled(fixedRateString = "${ips.incoming.scheduler.fixedRate:300000}")
    public void scheduledIncomingTransactionsPoll() {
        
        if (!schedulerEnabled) {
            LOG.debug("Incoming IPS scheduler is disabled");
            return;
        }

        LOG.info("║  SCHEDULED TASK: Polling IPS for incoming transactions        ║");

        try {
            int processedCount = fetchAndProcessIncomingTransactions();
            
            if (processedCount > 0) {
                LOG.info(" Scheduler successfully processed " + processedCount + " incoming transaction(s)");
            } else {
                LOG.info(" Scheduler completed - No new incoming transactions found");
            }
            
        } catch (BusinessException e) {
            LOG.error(" Scheduler failed with business error: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.error(" Scheduler failed with unexpected error", e);
        }

    }

    // =========================================================================
    // Point d'entrée principal (inchangé)
    // =========================================================================

    /**
     * Récupère toutes les transactions IPS, filtre celles dont le receiver
     * correspond à notre BIC (BKGFBIBIXXXX), puis traite chacune d'elles.
     *
     * @return nombre de transactions entrantes traitées
     * @throws Exception 
     */
    public int fetchAndProcessIncomingTransactions() throws Exception {

        LOG.info("=== Starting fetchAndProcessIncomingTransactions ===");

        // 1. Appeler l'API IPS pour obtenir toutes les transactions disponibles
        List<JsonNode> allTransactions = fetchAllTransactionsFromIPS();

        if (allTransactions == null || allTransactions.isEmpty()) {
            LOG.info("No transactions found in IPS output");
            return 0;
        }

        LOG.info("Total transactions from IPS: " + allTransactions.size());

        // 2. Filtrer uniquement les transactions dont on est le receiver
        List<JsonNode> ourTransactions = filterOurTransactions(allTransactions);

        LOG.info("Transactions matching our BIC (" + OUR_BIC + "): " + ourTransactions.size());

        if (ourTransactions.isEmpty()) {
            return 0;
        }

        // 3. Obtenir un token WalletCore une seule fois pour toutes les transactions
        String accessToken = getWalletCoreAccessToken();

        // 4. Traiter chaque transaction entrante
        int processed = 0;
        for (JsonNode tx : ourTransactions) {
            try {
                processIncomingTransaction(tx, accessToken);
                processed++;
            } catch (Exception e) {
                String traceRef = tx.has("traceReference") ? tx.get("traceReference").asText() : "UNKNOWN";
                LOG.error("Failed to process incoming transaction traceReference=" + traceRef, e);
                // On continue avec les autres transactions même si l'une échoue
            }
        }

        LOG.info("Successfully processed " + processed + "/" + ourTransactions.size() + " incoming transactions");
        return processed;
    }

    
    // =========================================================================

    private List<JsonNode> fetchAllTransactionsFromIPS() throws BusinessException {
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
        String responseBody = webRequest.getResponse().getResponseMsg();

        if (responseCode != 200) {
            LOG.error("IPS output returned HTTP " + responseCode + " : " + responseBody);
            //throw new BusinessException("IPS output endpoint returned error: HTTP " + responseCode);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            if (!root.isArray()) {
                LOG.error("IPS output response is not a JSON array");
                //throw new BusinessException("Unexpected IPS response format (not an array)");
            }

            List<JsonNode> list = new ArrayList<>();
            root.forEach(list::add);
            return list;

        } catch (Exception e) {
            LOG.error("Failed to parse IPS output JSON", e);
            //throw new BusinessException("Failed to parse IPS output response", e);
        }
		return null;
    }

    private List<JsonNode> filterOurTransactions(List<JsonNode> allTransactions) {
        List<JsonNode> result = new ArrayList<>();

        for (JsonNode tx : allTransactions) {
            if (!tx.has("receiver")) {
                continue;
            }
            String receiver = tx.get("receiver").asText("");
            if (OUR_BIC.equals(receiver) || receiver.startsWith("BKGFBIBI")) {
                result.add(tx);
            }
        }

        return result;
    }

    private void processIncomingTransaction(JsonNode txNode, String accessToken) throws Exception {
        String traceReference = txNode.has("traceReference") ? txNode.get("traceReference").asText() : "";
        String type = txNode.has("type") ? txNode.get("type").asText() : "";
        String documentXml = txNode.has("document") ? txNode.get("document").asText() : "";

        LOG.info("Processing incoming transaction: traceReference=" + traceReference + ", type=" + type);

        if (documentXml.isEmpty()) {
            LOG.warn("Empty document for traceReference=" + traceReference + ", skipping");
            return;
        }

        Map<String, String> txData = extractPacs008Fields(documentXml);
        txData.put("traceReference", traceReference);
        txData.put("messageType", type);

        LOG.info("Extracted pacs.008 fields for " + traceReference + " : " + txData);

        sendToWalletCoreBackend(txData, accessToken);
    }

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

        // AppHdr
        result.put("bizMsgIdr",     safeXpath(xpath, "//h:AppHdr/h:BizMsgIdr", doc));
        result.put("fromBic",       safeXpath(xpath, "//h:AppHdr/h:Fr/h:FIId/h:FinInstnId/h:BICFI", doc));
        result.put("toBic",         safeXpath(xpath, "//h:AppHdr/h:To/h:FIId/h:FinInstnId/h:BICFI", doc));
        result.put("creationDate",  safeXpath(xpath, "//h:AppHdr/h:CreDt", doc));
        result.put("bizProcDate",   safeXpath(xpath, "//h:AppHdr/h:BizPrcgDt", doc));

        // GrpHdr
        result.put("msgId",         safeXpath(xpath, "//p:GrpHdr/p:MsgId", doc));
        result.put("creDtTm",       safeXpath(xpath, "//p:GrpHdr/p:CreDtTm", doc));

        // PmtId
        result.put("instrId",       safeXpath(xpath, "//p:CdtTrfTxInf/p:PmtId/p:InstrId", doc));
        result.put("endToEndId",    safeXpath(xpath, "//p:CdtTrfTxInf/p:PmtId/p:EndToEndId", doc));
        result.put("txId",          safeXpath(xpath, "//p:CdtTrfTxInf/p:PmtId/p:TxId", doc));
        result.put("uetr",          safeXpath(xpath, "//p:CdtTrfTxInf/p:PmtId/p:UETR", doc));

        // PmtTpInf
        result.put("clearingChannel", safeXpath(xpath, "//p:CdtTrfTxInf/p:PmtTpInf/p:ClrChanl", doc));
        result.put("localInstrument", safeXpath(xpath, "//p:CdtTrfTxInf/p:PmtTpInf/p:LclInstrm/p:Prtry", doc));

        // Montant & devise
        result.put("amount",   safeXpath(xpath, "//p:CdtTrfTxInf/p:IntrBkSttlmAmt", doc));
        result.put("currency", safeXpathAttr(xpath, "//p:CdtTrfTxInf/p:IntrBkSttlmAmt/@Ccy", doc));
        result.put("settlementDate", safeXpath(xpath, "//p:CdtTrfTxInf/p:IntrBkSttlmDt", doc));

        // Agents
        result.put("instgAgt", getFinInstId(xpath, "//p:CdtTrfTxInf/p:InstgAgt/p:FinInstnId", doc));
        result.put("instdAgt", getFinInstId(xpath, "//p:CdtTrfTxInf/p:InstdAgt/p:FinInstnId", doc));
        result.put("dbtrAgt",  getFinInstId(xpath, "//p:CdtTrfTxInf/p:DbtrAgt/p:FinInstnId", doc));
        result.put("cdtrAgt",  getFinInstId(xpath, "//p:CdtTrfTxInf/p:CdtrAgt/p:FinInstnId", doc));

        // Débiteur
        result.put("debtorName",    safeXpath(xpath, "//p:CdtTrfTxInf/p:Dbtr/p:Nm", doc));
        result.put("debtorAccount", safeXpath(xpath, "//p:CdtTrfTxInf/p:DbtrAcct/p:Id/p:Othr/p:Id", doc));

        // Créditeur
        result.put("creditorName",    safeXpath(xpath, "//p:CdtTrfTxInf/p:Cdtr/p:Nm", doc));
        result.put("creditorAccount", safeXpath(xpath, "//p:CdtTrfTxInf/p:CdtrAcct/p:Id/p:Othr/p:Id", doc));

        // Purpose
        result.put("purpose", safeXpath(xpath, "//p:CdtTrfTxInf/p:Purp/p:Prtry", doc));

        // Remittance
        result.put("remittanceInfo", safeXpath(xpath, "//p:CdtTrfTxInf/p:RmtInf/p:Ustrd", doc));

        return result;
    }

    private static String getFinInstId(XPath xpath, String basePath, Document doc) {
        String bicfi = safeXpath(xpath, basePath + "/p:BICFI", doc);
        if (bicfi != null && !bicfi.isEmpty()) {
            return bicfi;
        }
        return safeXpath(xpath, basePath + "/p:ClrSysMmbId/p:MmbId", doc);
    }

    private static String safeXpath(XPath xpath, String expression, Document doc) {
        try {
            String value = xpath.evaluate(expression, doc);
            return value != null ? value.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static String safeXpathAttr(XPath xpath, String expression, Document doc) {
        return safeXpath(xpath, expression, doc);
    }
    
    /**
     * Builds a WalletPaymentBo from the data extracted from an ISO 20022 pacs.008 message.
     *
     * txData keys expected (extracted via XPath from pacs.008):
     *   - traceReference     : End-to-end transaction reference
     *   - creditorAccount    : IBAN/account number of the creditor (receiver)
     *   - debtorAccount      : IBAN/account number of the debtor (sender)
     *   - amount             : Transaction amount as String (e.g. "500.00")
     *   - currency           : ISO 4217 currency code (e.g. "333" or "USD")
     *   - debtorName         : Full name of the sender
     *   - creditorName       : Full name of the receiver
     *   - remittanceInfo     : Payment description/reason
     *   - walletDestination  : Wallet ID / phone number of the destination wallet
     */
    private WalletPaymentBo buildPaymentFromIsoData(Map<String, String> txData) {

        WalletPaymentBo payment = new WalletPaymentBo();

        // -- Core transaction fields (from PaymentBo parent) --
        payment.setAmount(
            parseAmount(txData.get("amount"))
        );
        payment.setCurrency(
            txData.get("currency")
        );
        payment.setAcquirerTrxRef(
            txData.get("traceReference")
        );
        payment.setDescription(
            txData.getOrDefault("remittanceInfo", "IPS Incoming Transfer")
        );
        payment.setCreateTime(new Date());

        // -- Account fields --
        // The creditor account is the receiver's account in our system
        payment.setFromAccountNumber(
            txData.get("creditorAccount")
        );
        payment.setAccountNumber(
            txData.getOrDefault("creditorAccount", "")
        );

        // -- Wallet routing --
        // walletDestination = the wallet ID/phone of the receiver
        payment.setWalletDestination(
            txData.get("walletDestination")
        );

        // -- Intent and handler (fixed values for IPS incoming) --
        payment.setIntent("inst_mobile_transfer");
        payment.setSendSourceHandler("BRB_IPS");

        // -- Commission --
        payment.setIsCommissionIncluded(false);

        // -- Optional: member ID if available in txData --
        if (txData.get("toMember") != null) {
            payment.setToMember(txData.get("toMember"));
        }

        LOG.debug("Built WalletPaymentBo from IPS data: " + payment.toString());

        return payment;
    }

    /**
     * Safely parses amount string to Double.
     * Returns 0.0 if null or invalid.
     */
    private Double parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            LOG.warn("Amount is null or empty in ISO 20022 data, defaulting to 0.0");
            return 0.0;
        }
        try {
            return Double.parseDouble(amountStr.trim());
        } catch (NumberFormatException e) {
            LOG.error("Cannot parse amount: '" + amountStr + "', defaulting to 0.0");
            return 0.0;
        }
    }

    
    private void sendToWalletCoreBackend(Map<String, String> txData, String accessToken) throws Exception {

        // ============================================
        // 1. BUILD THE WalletPaymentBo FROM ISO 20022
        // ============================================
        WalletPaymentBo payment = buildPaymentFromIsoData(txData);


        // ============================================
        // 3. BUILD AND SEND THE HTTP REQUEST
        // ============================================
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

        // ============================================
        // 4. HANDLE RESPONSE
        // ============================================
        int code = req.getResponse().getResponseCode();
        String responseMsg = req.getResponse().getResponseMsg();

        if (code != 200 && code != 201) {
            LOG.error("WalletCore rejected incoming IPS transaction."
                    + " HTTP=" + code
                    + ", traceRef=" + txData.get("traceReference")
                    + ", creditorAccount=" + txData.get("creditorAccount")
                    + ", response=" + responseMsg);
            throw new BusinessException(
                    "WalletCore rejected incoming IPS transaction HTTP " + code
                    + " | ref=" + txData.get("traceReference"));
        }

        LOG.info("Incoming IPS transaction successfully sent to WalletCore:"
                + " traceRef=" + txData.get("traceReference")
                + ", creditorAccount=" + txData.get("creditorAccount")
                + ", amount=" + txData.get("amount")
                + " " + txData.get("currency"));
    }

    private String getWalletCoreAccessToken() throws Exception {
        WebRequest tokenRequest = buildTokenRequest();
        WebInterface.processRequest(tokenRequest);

        if (tokenRequest.getResponse().getResponseCode() != 200) {
            LOG.error("WalletCoreAuthentication :: error getting access token: "
                    + tokenRequest.getResponse());
            throw new BusinessException("Error getting WalletCore access token");
        }

        String tokenJson = tokenRequest.getResponse().getResponseMsg();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode tokenNode = mapper.readTree(tokenJson);
        String accessToken = tokenNode.get("access_token").asText();

        LOG.info("WalletCore access token obtained successfully");
        return accessToken;
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
