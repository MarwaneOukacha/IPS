package com.paylogic.ips.services;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gms.utils.common.StringUtil;
import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.WebInterface;
import com.gms.utils.net.webinterface.WebRequest;
import com.gms.utils.net.webinterface.WebRequest.QUERY_METHOD;
import com.paylogic.ips.bo.DocumentTokenResponseBo;
import com.paylogic.ips.bo.IpsRTPPaymentRequestBo;
import com.paylogic.ips.bo.IpsRTPSendPaymentRequestBo;
import com.paylogic.ips.bo.PaymentRTPRequestBo;
import com.paylogic.ips.util.BrbBicRepository;

@Service
public class ServiceRTP {

    private static final Logger LOG = Logger.getLogger(ServiceRTP.class);

    private static final String OUR_BIC       = "BKGFBIBIXXXX";
    private static final String OUR_BIC_SHORT = "BKGFBIBI";
    private static final String IPS_RECEIVER  = "BRBUBIBAXIPS";

    @Autowired
    private TokenService tokenService;

    @Value("${walletcore.url.acceptType:application/json}")
    private String acceptType;

    @Value("${walletcore.url.connectTimeout:10000}")
    private int connectTimeout;

    @Value("${walletcore.url.readTimeout:10000}")
    private int readTimeout;

    @Value("${checkOutpayment}")
    private String checkOutpayment;

    @Value("${rtp}")
    private String rtp;

    @Value("${ips.incoming.input.url}")
    private String ipsInputUrl;

    @Value("${user}")
    private String user;

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Orchestrates the full RTP payment flow:
     * 1. Calls start-of-payment to obtain the document token.
     * 2. Injects the token (+ UETR when present) into the request BO.
     * 3. Sends the pacs.008 message to IPS.
     */
    public void initiateRTPPayment(IpsRTPSendPaymentRequestBo requestBo)
            throws BusinessException {

        LOG.info("Initiating RTP payment : " + requestBo);

        // Step 1 – start-of-payment
        DocumentTokenResponseBo tokenResponse =
                startPayment(requestBo.getPaymentRTPRequestBo(), requestBo.getUetr());

        // Step 2 – enrich the request BO with values from the token response
        requestBo.setDocumentToken(tokenResponse.getDocumentToken());


        LOG.info("Document token obtained: " + tokenResponse.getDocumentToken());

        // Step 3 – send pacs.008 to IPS
        sendPacs008ToIPS(requestBo);
    }

    /** Polls the RTP output queue and processes incoming pain.013 messages. */
    public String receivePain013() throws BusinessException {

        String url = checkOutpayment + UUID.randomUUID() + "?service=rtp";
        LOG.info("Calling RTP output URL: " + url);

        WebRequest webRequest = buildGetRequest(url);
        executeRequest(webRequest, "RTP output");

        int responseCode = webRequest.getResponse().getResponseCode();
        String responseBody = webRequest.getResponse().getResponseMsg();
        
        
        
        if (responseCode!= 200) {
            LOG.error("RTP output returned HTTP " + responseCode + ": " + responseBody);
            JSONObject jsonResponse = new JSONObject(responseBody);
            throw new BusinessException(
            		jsonResponse.optString("description")
            );
        }
        
        return responseBody;
        //parseAndProcessTransactions(responseBody);
    }

    /** Calls the start-of-payment endpoint and returns the token response. */
    public DocumentTokenResponseBo startPayment(PaymentRTPRequestBo bo, String requestUuid)
            throws BusinessException {

        String url = rtp + requestUuid + "/start-of-payment";
        LOG.info("Calling RTP start payment URL: " + url);

        LOG.info("RTP start payment request body: " + bo);

        WebRequest webRequest = buildPostJsonRequest(url, bo);
        executeRequest(webRequest, "RTP start payment");

        int responseCode = webRequest.getResponse().getResponseCode();
        String responseBody = webRequest.getResponse().getResponseMsg();
        LOG.info("RTP start payment – HTTP " + responseCode + " | body: " + responseBody);
        

        if (responseCode!= 200) {
            LOG.error("RTP output returned HTTP " + responseCode + ": " + responseBody);
            JSONObject jsonResponse = new JSONObject(responseBody);
            throw new BusinessException(
            		jsonResponse.optString("description")
            );
        }

        try {
            DocumentTokenResponseBo responseBo =
                    new ObjectMapper().readValue(responseBody, DocumentTokenResponseBo.class);
            LOG.info("Parsed start-payment response: " + responseBo);
            return responseBo;
        } catch (Exception e) {
            LOG.error("Failed to parse RTP start payment response", e);
            throw new BusinessException("Failed to parse RTP start payment response", e);
        }
    }

    /** Builds the pacs.008 payload and posts it to the IPS input endpoint. */
    public void sendPacs008ToIPS(IpsRTPSendPaymentRequestBo requestBo)
            throws BusinessException {

        String requestUuid  = UUID.randomUUID().toString();
        IpsRTPPaymentRequestBo request = buildIpsRTPRequest(requestBo);
        String url = ipsInputUrl + requestUuid + "?service=ips";

        LOG.info("Calling IPS input URL: " + url);
        LOG.info("IPS pacs.008 request body: " + request);

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
    
    public void sendPain013ToIPS(IpsRTPSendPaymentRequestBo requestBo)
            throws BusinessException {

        String requestUuid  = UUID.randomUUID().toString();
        IpsRTPPaymentRequestBo request = buildRTPRequestToPay(requestBo);
        String url = ipsInputUrl + requestUuid + "?service=rtp";

        LOG.info("Calling IPS input URL: " + url);
        LOG.info("IPS pain.013 request body: " + request);

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

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS – HTTP
    // ─────────────────────────────────────────────────────────────────────────

    private WebRequest buildGetRequest(String url) throws BusinessException {
        WebRequest req = baseRequest(url);
        req.setQueryMethod(QUERY_METHOD.GET);
        return req;
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

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS – PARSING
    // ─────────────────────────────────────────────────────────────────────────

    private void parseAndProcessTransactions(String responseBody) throws BusinessException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            if (!root.isArray()) {
                LOG.error("RTP output response is not a JSON array");
                return;
            }

            List<JsonNode> list = new ArrayList<>();
            root.forEach(list::add);

            for (JsonNode transaction : list) {
                LOG.info("Received RTP transaction: " + transaction);
                // TODO: process each pain.013 message here
            }
        } catch (Exception e) {
            LOG.error("Failed to parse RTP output JSON", e);
            throw new BusinessException("Failed to parse RTP output JSON", e);
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

    private String toJson(Object obj) throws BusinessException {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new BusinessException("Failed to serialize object to JSON", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS – PAYLOAD BUILDER
    // ─────────────────────────────────────────────────────────────────────────

    private IpsRTPPaymentRequestBo buildIpsRTPRequest(IpsRTPSendPaymentRequestBo requestBo)
            throws BusinessException {

        PaymentRTPRequestBo paymentRequest = requestBo.getPaymentRTPRequestBo();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("+02:00"));
        String creationDateTime = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String dateOnly         = now.toLocalDate().toString();

        String traceReference  = requestBo.getTraceReference();
        String amount          = paymentRequest.getPaymentAmount().getSum().toString();
        String debtorAccount   = requestBo.getDebtorAccount();
        String creditorAccount = requestBo.getCreditorAccount();
        String documentToken   = requestBo.getDocumentToken();
        String uetr            = requestBo.getUetr();
        String receiverBic            = requestBo.getReceverBic();

        validateRequired(traceReference, "traceReference");
        validateRequired(documentToken,  "documentToken");
        validateRequired(uetr,           "uetr");

        String xml = buildPacs008Xml(
                traceReference, creationDateTime, dateOnly, amount,
                requestBo.getDebtorName(), debtorAccount,
                requestBo.getCreditorName(), creditorAccount,
                uetr,receiverBic);

        IpsRTPPaymentRequestBo request = new IpsRTPPaymentRequestBo();
        request.setTraceReference(traceReference);
        request.setType("pacs.008.001.10");
        request.setSender(user);
        request.setReceiver(IPS_RECEIVER);
        request.setDocumentToken(documentToken);
        request.setDocument(xml);
        return request;
    }
    private IpsRTPPaymentRequestBo buildRTPRequestToPay(IpsRTPSendPaymentRequestBo requestBo)
            throws BusinessException {

        PaymentRTPRequestBo paymentRequest = requestBo.getPaymentRTPRequestBo();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("+02:00"));

        String creationDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        // → "2026-05-20T16:11:33.632+02:00"

        String expiryDateTime = now.plusMinutes(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        // → "2026-05-20T16:41:33.632+02:00"

        String traceReference  = requestBo.getTraceReference();
        String amount          = paymentRequest.getPaymentAmount().getSum().toString();
        String debtorAccount   = requestBo.getDebtorAccount();
        String creditorAccount = requestBo.getCreditorAccount();
        String uetr            = requestBo.getUetr();

        validateRequired(traceReference, "traceReference");
        validateRequired(uetr,           "uetr");

        String xml = buildPain013Xml(
                traceReference, creationDateTime, expiryDateTime, amount,
                requestBo.getDebtorName(), debtorAccount,
                requestBo.getCreditorName(), creditorAccount,
                OUR_BIC_SHORT,requestBo.getReceverBic(),uetr);

        IpsRTPPaymentRequestBo request = new IpsRTPPaymentRequestBo();
        request.setTraceReference(traceReference);
        request.setType("pain.013.001.09");
        request.setSender(user);
        request.setReceiver(IPS_RECEIVER);
        request.setDocument(xml);
        return request;
    }
    

    private void validateRequired(String value, String fieldName) throws BusinessException {
        if (StringUtil.isNullOrEmpty(value)) {
            throw new BusinessException(fieldName + " is required");
        }
    }

    private String buildPacs008Xml(
            String traceReference, String creationDateTime, String dateOnly,
            String amount, String debtorName, String debtorAccount,
            String creditorName, String creditorAccount,
            String uetr, String receiverBic) {

        boolean internal = isInternalBic(receiverBic);

        String toBic = internal
                ? "<BICFI>" + receiverBic + "</BICFI>"
                : "<ClrSysMmbId><MmbId>" + receiverBic + "</MmbId></ClrSysMmbId>";

        return new StringBuilder()
            .append("<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">")
            .append("<Body>")
                .append("<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">")
                    .append("<Fr><FIId><FinInstnId><BICFI>").append(OUR_BIC_SHORT).append("</BICFI></FinInstnId></FIId></Fr>")

                    // 🔥 ONLY CHANGE HERE
                    .append("<To><FIId><FinInstnId>").append(toBic).append("</FinInstnId></FIId></To>")

                    .append("<BizMsgIdr>").append(traceReference).append("</BizMsgIdr>")
                    .append("<MsgDefIdr>pacs.008.001.10</MsgDefIdr>")
                    .append("<BizSvc>brb.ips.01</BizSvc>")
                    .append("<CreDt>").append(creationDateTime).append("</CreDt>")
                    .append("<Prty>0100</Prty>")
                .append("</AppHdr>")
                .append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.10\">")
                    .append("<FIToFICstmrCdtTrf>")
                        .append("<GrpHdr>")
                            .append("<MsgId>").append(traceReference).append("</MsgId>")
                            .append("<CreDtTm>").append(creationDateTime).append("</CreDtTm>")
                            .append("<NbOfTxs>1</NbOfTxs>")
                            .append("<SttlmInf><SttlmMtd>CLRG</SttlmMtd></SttlmInf>")
                        .append("</GrpHdr>")
                        .append("<CdtTrfTxInf>")
                            .append("<PmtId>")
                                .append("<InstrId>").append(traceReference).append("</InstrId>")
                                .append("<EndToEndId>NOTPROVIDED</EndToEndId>")
                                .append("<TxId>").append(traceReference).append("</TxId>")
                                .append("<UETR>").append(uetr).append("</UETR>")
                            .append("</PmtId>")
                            .append("<PmtTpInf>")
                                .append("<ClrChanl>RTNS</ClrChanl>")
                                .append("<LclInstrm><Prtry>PMNI</Prtry></LclInstrm>")
                            .append("</PmtTpInf>")
                            .append("<IntrBkSttlmAmt Ccy=\"BIF\">").append(amount).append("</IntrBkSttlmAmt>")
                            .append("<IntrBkSttlmDt>").append(dateOnly).append("</IntrBkSttlmDt>")
                            .append("<ChrgBr>SLEV</ChrgBr>")

                            .append("<InstgAgt><FinInstnId><BICFI>")
                                .append(OUR_BIC_SHORT)
                            .append("</BICFI></FinInstnId></InstgAgt>")

                            .append("<InstdAgt><FinInstnId>").append(toBic).append("</FinInstnId></InstdAgt>")

                            .append("<Dbtr><Nm>").append(debtorName).append("</Nm></Dbtr>")
                            .append("<DbtrAcct><Id><Othr><Id>").append(debtorAccount).append("</Id></Othr></Id></DbtrAcct>")
                            .append("<DbtrAgt><FinInstnId><BICFI>").append(OUR_BIC_SHORT).append("</BICFI></FinInstnId></DbtrAgt>")

                            .append("<CdtrAgt><FinInstnId>").append(toBic).append("</FinInstnId></CdtrAgt>")

                            .append("<Cdtr><Nm>").append(creditorName).append("</Nm></Cdtr>")
                            .append("<CdtrAcct><Id><Othr><Id>").append(creditorAccount).append("</Id></Othr></Id></CdtrAcct>")
                            .append("<Purp><Prtry>007</Prtry></Purp>")
                            .append("<RmtInf>")
                                .append("<Ustrd>Informations de la remise</Ustrd>")
                                .append("<Strd><RfrdDocInf><Tp><CdOrPrtry><Prtry>RRTP</Prtry></CdOrPrtry></Tp></RfrdDocInf></Strd>")
                            .append("</RmtInf>")
                        .append("</CdtTrfTxInf>")
                    .append("</FIToFICstmrCdtTrf>")
                .append("</Document>")
            .append("</Body>")
            .append("</DataPDU>")
            .toString();
    }
    
    
    
    
    
    
    private String buildPain013Xml(
            String traceReference, String creationDateTime, String expiryDateTime,
            String amount, String debtorName, String debtorAccount,
            String creditorName, String creditorAccount,
            String senderBic, String receiverBic, String uetr) {
    	
    	 boolean internal = isInternalBic(receiverBic);

         String toBic = internal
                 ? "<BICFI>" + receiverBic + "</BICFI>"
                 : "<ClrSysMmbId><MmbId>" + receiverBic + "</MmbId></ClrSysMmbId>";

        return new StringBuilder()
            .append("<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">")
            .append("<Body>")

                .append("<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">")

                    .append("<Fr>")
                        .append("<FIId>")
                            .append("<FinInstnId>")                          // *** no <BICFI> wrapper ***
                                .append("<BICFI>").append(senderBic).append("</BICFI>")
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
                    .append("<MsgDefIdr>pain.013.001.09</MsgDefIdr>")
                    .append("<BizSvc>brb.ips.01</BizSvc>")
                    .append("<CreDt>").append(creationDateTime).append("</CreDt>")
                    .append("<Prty>0100</Prty>")

                .append("</AppHdr>")

                .append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.013.001.09\">")

                    .append("<CdtrPmtActvtnReq>")

                        .append("<GrpHdr>")
                            .append("<MsgId>").append(traceReference).append("</MsgId>")
                            .append("<CreDtTm>").append(creationDateTime).append("</CreDtTm>")
                            .append("<NbOfTxs>1</NbOfTxs>")
                            .append("<InitgPty>")
                                .append("<Id>")
                                    .append("<OrgId>")
                                        .append("<AnyBIC>").append(senderBic).append("</AnyBIC>")
                                    .append("</OrgId>")
                                .append("</Id>")
                            .append("</InitgPty>")
                        .append("</GrpHdr>")

                        .append("<PmtInf>")

                            .append("<PmtInfId>").append(traceReference).append("</PmtInfId>")
                            .append("<PmtMtd>TRF</PmtMtd>")

                            .append("<ReqdExctnDt>")
                                .append("<DtTm>").append(creationDateTime).append("</DtTm>")
                            .append("</ReqdExctnDt>")

                            .append("<XpryDt>")
                                .append("<DtTm>").append(expiryDateTime).append("</DtTm>")
                            .append("</XpryDt>")

                            .append("<Dbtr>")
                                .append("<Nm>").append(debtorName).append("</Nm>")
                            .append("</Dbtr>")

                            .append("<DbtrAcct>")
                                .append("<Id>")
                                    .append("<Othr>")
                                        .append("<Id>").append(debtorAccount).append("</Id>")
                                    .append("</Othr>")
                                .append("</Id>")
                            .append("</DbtrAcct>")

                            // *** DbtrAgt: <FinInstnId> contains <BICFI> directly ***
                            .append("<DbtrAgt>")
                                .append("<FinInstnId>")
                                    .append(toBic)
                                .append("</FinInstnId>")
                            .append("</DbtrAgt>")

                            .append("<CdtTrfTx>")

                                .append("<PmtId>")
                                    .append("<InstrId>").append(traceReference).append("</InstrId>")
                                    .append("<EndToEndId>NOTPROVIDED</EndToEndId>")
                                    .append("<UETR>").append(uetr).append("</UETR>")
                                .append("</PmtId>")

                                .append("<PmtTpInf>")
                                    .append("<LclInstrm>")
                                        .append("<Prtry>PMNI</Prtry>")
                                    .append("</LclInstrm>")
                                .append("</PmtTpInf>")

                                .append("<Amt>")
                                    .append("<InstdAmt Ccy=\"BIF\">").append(amount).append("</InstdAmt>")
                                .append("</Amt>")

                                .append("<ChrgBr>SLEV</ChrgBr>")

                                // *** CdtrAgt: <FinInstnId> contains <BICFI> directly ***
                                .append("<CdtrAgt>")
                                    .append("<FinInstnId>")
                                        .append("<BICFI>").append(senderBic).append("</BICFI>")
                                    .append("</FinInstnId>")
                                .append("</CdtrAgt>")

                                .append("<Cdtr>")
                                    .append("<Nm>").append(creditorName).append("</Nm>")
                                .append("</Cdtr>")

                                .append("<CdtrAcct>")
                                    .append("<Id>")
                                        .append("<Othr>")
                                            .append("<Id>").append(creditorAccount).append("</Id>")
                                        .append("</Othr>")
                                    .append("</Id>")
                                .append("</CdtrAcct>")

                                .append("<Purp>")
                                    .append("<Prtry>007</Prtry>")
                                .append("</Purp>")

                            .append("</CdtTrfTx>")

                        .append("</PmtInf>")

                    .append("</CdtrPmtActvtnReq>")

                .append("</Document>")

            .append("</Body>")
            .append("</DataPDU>")
            .toString();
    }
    

	public void initiateRTPRequest(IpsRTPSendPaymentRequestBo ipsRTPSendPaymentRequestBo) throws BusinessException {
		// TODO Auto-generated method stub
		sendPain013ToIPS(ipsRTPSendPaymentRequestBo);
		
	}
	
	private boolean isInternalBic(String receiverBic) {
	    return BrbBicRepository.isValidBic(receiverBic);
	}
}