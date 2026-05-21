package com.paylogic.ips.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.spi.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.WebInterface;
import com.gms.utils.net.webinterface.WebRequest;
import com.gms.utils.net.webinterface.WebRequest.QUERY_METHOD;
import com.paylogic.ama.core.model.AccountInfo;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ips.services.IncomingIpsService;
import com.paylogic.ips.util.CoreUtil;

import org.apache.log4j.Logger;

@RestController
@RequestMapping("/incomming")
public class IncomingController {

	private static final Logger LOG = Logger.getLogger(InternalController.class.getName());

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

    @Value("${ips.incoming.output.url}")
    private String ipsOutputUrl;
    private static final List<String> ALLOWED_ACK_STATUSES = Arrays.asList("AUTH", "NAUT", "SUSP");

    @Autowired
    private IncomingIpsService incomingIpsService;
    
    @GetMapping("/get")
    public  List<JsonNode> testSendPacs002() throws BusinessException {
    	return incomingIpsService.fetchAllTransactionsFromIPS();
    }
    
    
/*
@PostMapping("/pacs002")
public String testSendPacs002() {

    // ---- Hardcoded txData from real log ----
    Map<String, String> txData = new HashMap<>();
    txData.put("traceReference",  "BUCIBIBIAXXX-20260424T121353");
    txData.put("msgId",           "BUCIBIBIAXXX-20260424T121353");
    txData.put("creDtTm",         "2026-04-24T12:13:53+02:00");
    txData.put("fromBic",         "BKGFBIBI");
    txData.put("settlementDate",  "2026-04-24");
    txData.put("amount",          "1632");
    txData.put("currency",        "BIF");
    txData.put("debtorAccount",   "58950792436");
    txData.put("debtorName",      "Nadia Martin");
    txData.put("creditorAccount", "699999999");
    txData.put("creditorName",    "HERMENEGILDE");
    txData.put("purpose",         "002");
    txData.put("remittanceInfo",  "Informations de la remise");
    txData.put("instrId",         "BUCIBIBIAXXX-20260424T121353");
    txData.put("txId",            "BUCIBIBIAXXX-20260424T121353");
    txData.put("endToEndId",      "NOTPROVIDED");
    txData.put("instgAgt",        "BUCIBIBI");
    txData.put("instdAgt",        "BKGFBIBI");
    txData.put("dbtrAgt",         "BUCIBIBI");
    txData.put("cdtrAgt",         "BKGFBIBI");
    txData.put("toBic",           "BKGFBIBI");
    txData.put("bizMsgIdr",       "3080457");
    txData.put("clearingChannel", "RTNS");
    txData.put("localInstrument", "TRFI");
    txData.put("messageType",     "pacs.008.001.10");
    txData.put("jsonSender",      "BRBUBIBAAIPS");
    txData.put("creationDate",    "2026-04-24T12:14:01+02:00");

    // ---- Desired ack status ----
    String ackStatus = "NAUT"; // Change to "RJCT" or "PDNG" as needed

    LOG.info("TEST ENDPOINT: Sending hardcoded pacs.002 for traceRef="
            + txData.get("traceReference") + ", ackStatus=" + ackStatus);

    // ---- Basic validation ----
    if (txData.get("traceReference") == null || txData.get("traceReference").isEmpty()) {
        return "ERROR: 'traceReference' is required";
    }
    if (txData.get("fromBic") == null || txData.get("fromBic").isEmpty()) {
        return "ERROR: 'fromBic' is required";
    }
    if (!ALLOWED_ACK_STATUSES.contains(ackStatus)) {
        return "ERROR: Invalid ackStatus. Allowed values: " + ALLOWED_ACK_STATUSES;
    }

    // ---- Dispatch ----
    try {
        incomingIpsService.sendPacs002Acknowledgment(txData, ackStatus);

        LOG.info("TEST ENDPOINT: pacs.002 dispatched for traceRef="
                + txData.get("traceReference") + ", ackStatus=" + ackStatus);

        return "OK: pacs.002 dispatched - traceRef=" + txData.get("traceReference")
                + ", ackStatus=" + ackStatus
                + " (check logs for IPS HTTP response)";

    } catch (Exception e) {
        LOG.error("TEST ENDPOINT: Failed to dispatch pacs.002", e);
        return "ERROR: " + e.getMessage();
    }
}

	

	
    
    // ============================================================
    // POST /test  -  accepts txData in request body
    // ============================================================
    @PostMapping("/test")
    public void test() throws IOException {

        // ============================================
        // 1. OBTAIN ACCESS TOKEN
        // ============================================
        WebRequest tokenRequest = buildTokenRequest();
        WebInterface.processRequest(tokenRequest);

        String tokenResponse = tokenRequest.getResponse().getResponseMsg();
        LOG.info("Token response: "+ tokenResponse);

        String accessToken = extractAccessToken(tokenResponse);
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IOException("Failed to retrieve access token. Response: " + tokenResponse);
        }

        
        Payment payment = new Payment();
        
        payment.setAmount(1002.2);
        payment.setIntent("inst_wallet_to_acc");
        payment.setCurrency("108");
        payment.setFromMember("BRBUBIBAIPS");
        payment.setToMember("20005");
        payment.setState("ACCEPTED");
        payment.setVoucherCode(CoreUtil.generateVoucher("AN", 9));
        payment.setSendSourceHandler("BRB_IPS");
        payment.setAcquirerTrxRef("0355556933256");
        payment.setCreateTime(new Date());
        
        List<AccountInfo> dstAccounts = new ArrayList<>();
        AccountInfo accountDst = new AccountInfo();
        
        
        accountDst.setIden("13000005495");
        accountDst.setName("dest");
        accountDst.setType("ACCOUNT");
        payment.setWalletSource("700000693");
        dstAccounts.add(accountDst);
        
        payment.setDstAccounts(dstAccounts);
        // ============================================
        // 3. BUILD AND SEND THE PAYMENT HTTP REQUEST
        // ============================================
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        
        WebRequest paymentRequest = new WebRequest();
        paymentRequest.setAcceptType(acceptType);
        paymentRequest.setMediaType(mediaTypeJson);
        paymentRequest.setHeader(headers);
        paymentRequest.setReadTimeout(readTimeout);
        paymentRequest.setConnectTimeout(connectTimeout);
        paymentRequest.setUrl("https://192.168.0.223:8463/amawalletmanagerws/v1/ama/payment/send");
        paymentRequest.setQueryMethod(QUERY_METHOD.POST);
        paymentRequest.setBody(payment);

        WebInterface.processRequest(paymentRequest);

        // ============================================
        // 4. HANDLE PAYMENT RESPONSE
        // ============================================
        int code             = paymentRequest.getResponse().getResponseCode();
        String paymentResMsg = paymentRequest.getResponse().getResponseMsg();

        LOG.info("Payment response HTTP= "+ paymentResMsg);

        if (code != 200 && code != 201) {
            
            throw new IOException(
                    "WalletCore rejected incoming IPS transaction HTTP " + code);
        }

        LOG.info("Payment accepted successfully. issuertrxref= ");
    }

    // ============================================================
    // Build OAuth token request
    // ============================================================
    private WebRequest buildTokenRequest() {
        WebRequest req = new WebRequest();
        req.setAcceptType(acceptType);
        req.setMediaType(mediaTypeForm);
        req.setReadTimeout(readTimeout);
        req.setConnectTimeout(connectTimeout);
        req.setUrl("https://192.168.0.223:8463/amawalletmanagerws/oauth/token");
        req.setQueryMethod(QUERY_METHOD.POST);
        req.setBody("grant_type=password&client_id=restapp&client_secret=restapp&scope=read&username=ips&password=payway100");
        return req;
    }

    // ============================================================
    // Extract access_token from OAuth JSON response
    // e.g. {"access_token":"abc123","token_type":"bearer",...}
    // ============================================================
    private String extractAccessToken(String responseMsg) {
        if (responseMsg == null || responseMsg.isEmpty()) {
            return null;
        }
        try {
            // Simple substring extraction — no extra library needed
            String key = "\"access_token\"";
            int keyIdx = responseMsg.indexOf(key);
            if (keyIdx == -1) return null;

            int colonIdx  = responseMsg.indexOf(":", keyIdx);
            int quoteOpen = responseMsg.indexOf("\"", colonIdx);
            int quoteClose = responseMsg.indexOf("\"", quoteOpen + 1);

            return responseMsg.substring(quoteOpen + 1, quoteClose);

        } catch (Exception e) {
            LOG.error("Failed to parse access_token from response: "+ responseMsg, e);
            return null;
        }
    }
    */
}
