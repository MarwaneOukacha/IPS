package com.paylogic.ips.services;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gms.utils.common.StringUtil;
import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.WebInterface;
import com.gms.utils.net.webinterface.WebRequest;
import com.gms.utils.net.webinterface.WebRequest.QUERY_METHOD;
import com.paylogic.ips.bo.DocumentTokenResponseBo;
import com.paylogic.ips.bo.IpsQrcodeSendPaymentRequestBo;
import com.paylogic.ips.bo.IpsRTPPaymentRequestBo;
import com.paylogic.ips.bo.QrCodeRequestDto;
import com.paylogic.ips.bo.QrCodeResponseDto;
import com.paylogic.ips.bo.QrPaymentResponseBo;
import com.paylogic.ips.util.BrbBicRepository;

@Service
public class ServiceQrcode {

    private static final Logger LOG = Logger.getLogger(ServiceQrcode.class);

    private static final String OUR_BIC_SHORT = "BKGFBIBI";
    private static final String IPS_RECEIVER  = "BRBUBIBAXIPS";
    private static final String CREDITOR_BIC  = "BRBUBIBI";

    @Autowired
    private TokenService tokenService;

    @Value("${walletcore.url.connectTimeout:10000}")
    private int connectTimeout;

    @Value("${walletcore.url.readTimeout:10000}")
    private int readTimeout;

    @Value("${readQrcode}")
    private String readQrcode;



    @Value("${ips.incoming.input.url}")
    private String ipsInputUrl;
    
    @Value("${ips.qrcode.start.payment}")
    private String ipsqrCodeUrl;
    
    @Value("${ips.qrcode.create}")
    private String createQrCodeUrl;

    @Value("${user}")
    private String user;

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Orchestrates the full QR code payment flow:
     * 1. Calls start-of-payment to obtain the document token.
     * 2. Injects the token into the request BO.
     * 3. Sends the pacs.008 message to IPS.
     */
    public void initiateQrcodePayment(IpsQrcodeSendPaymentRequestBo requestBo)
            throws BusinessException {

        LOG.info("Initiating QR code payment – uetr: " + requestBo.getUetr());

        // Step 1 – start-of-payment
        DocumentTokenResponseBo tokenResponse =
                startPayment(requestBo.getPaymentQrcodeRequestBo(), requestBo.getQrExtensionUUID());

        // Step 2 – enrich the request BO with the document token
        requestBo.setDocumentToken(tokenResponse.getDocumentToken());

        LOG.info("Document token obtained: " + tokenResponse.getDocumentToken());

        // Step 3 – send pacs.008 to IPS
        sendPacs008ToIPS(requestBo);
    }
    
    public QrCodeResponseDto createQrCode(QrCodeRequestDto qrCodeRequestDto) throws BusinessException {
		// TODO Auto-generated method stub
		LOG.info("Creating QR code  : " + qrCodeRequestDto);
		WebRequest webRequest = buildPostJsonRequest(createQrCodeUrl,qrCodeRequestDto);
        executeRequest(webRequest, "QR Code creation");
        
        int responseCode = webRequest.getResponse().getResponseCode();
        String responseBody = webRequest.getResponse().getResponseMsg();
        LOG.info("QR Code Infos – HTTP " + responseCode + " | body: " + responseBody);

        
        JSONObject jsonResponse = new JSONObject(responseBody);
        
        if (responseCode!= 200) {
            throw new BusinessException(
            		jsonResponse.optString("description")
            );
        }

        try {
            return new ObjectMapper().readValue(responseBody, QrCodeResponseDto.class);
        } catch (Exception e) {
            LOG.error("Failed to parse QR Code response response", e);
            throw new BusinessException("Failed to parse QR Code response response", e);
        }
	}

    /** Fetches QR code payment information by UUID. */
    public QrPaymentResponseBo getQrCodeInfos(String uuid) throws BusinessException {

        String url = readQrcode + uuid;
        LOG.info("Calling QR Code Infos URL: " + url);

        WebRequest webRequest = buildGetRequest(url);
        executeRequest(webRequest, "QR Code Infos");

        int responseCode = webRequest.getResponse().getResponseCode();
        String responseBody = webRequest.getResponse().getResponseMsg();
        LOG.info("QR Code Infos – HTTP " + responseCode + " | body: " + responseBody);

        JSONObject jsonResponse = new JSONObject(responseBody);
        
        if (responseCode!= 200) {
            throw new BusinessException(
            		jsonResponse.optString("description")
            );
        }

        try {
            return new ObjectMapper().readValue(responseBody, QrPaymentResponseBo.class);
        } catch (Exception e) {
            LOG.error("Failed to parse QR Code Infos response", e);
            throw new BusinessException("Failed to parse QR Code Infos response", e);
        }
    }

    /** Calls the start-of-payment endpoint and returns the token response. */
    public DocumentTokenResponseBo startPayment(Object bo, String requestUuid)
            throws BusinessException {

        String url = ipsqrCodeUrl + requestUuid + "/start-of-payment";
        LOG.info("Calling QR start payment URL: " + url);
        LOG.info("QR start payment request body: " + bo);

        WebRequest webRequest = buildPostJsonRequest(url, bo);
        executeRequest(webRequest, "QR start payment");

        int responseCode = webRequest.getResponse().getResponseCode();
        String responseBody = webRequest.getResponse().getResponseMsg();
        LOG.info("QR start payment – HTTP " + responseCode + " | body: " + responseBody);

        JSONObject jsonResponse = new JSONObject(responseBody);
        
        if (responseCode!= 200) {
            throw new BusinessException(
            		jsonResponse.optString("description")
            );
        }

        try {
            DocumentTokenResponseBo responseBo =
                    new ObjectMapper().readValue(responseBody, DocumentTokenResponseBo.class);
            LOG.info("Parsed QR start-payment response: " + responseBo);
            return responseBo;
        } catch (Exception e) {
            LOG.error("Failed to parse QR start payment response", e);
            throw new BusinessException("Failed to parse QR start payment response", e);
        }
    }

    /** Builds the pacs.008 payload and posts it to the IPS input endpoint. */
    public void sendPacs008ToIPS(IpsQrcodeSendPaymentRequestBo requestBo)
            throws BusinessException {

        //String requestUuid = UUID.randomUUID().toString();
        IpsRTPPaymentRequestBo request = buildIpsQrcodeRequest(requestBo);
        String url = ipsInputUrl + requestBo.getUetr() + "?service=ips";

        LOG.info("Calling IPS input URL: " + url);
        LOG.info("IPS pacs.008 request body: " + request);

        WebRequest webRequest = buildPostJsonRequest(url, request);
        executeRequest(webRequest, "IPS input");

        int responseCode = webRequest.getResponse().getResponseCode();
        String responseBody = webRequest.getResponse().getResponseMsg();
        LOG.info("IPS input – HTTP " + responseCode + " | body: " + responseBody);

        JSONObject jsonResponse = new JSONObject(responseBody);
        
        if (responseCode!= 200) {
            throw new BusinessException(
            		jsonResponse.optString("description")
            );
        }

        //logParsedJson(responseBody, "IPS input response");
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

    private void logParsedJson(String responseBody, String label) throws BusinessException {
        try {
            JsonNode json = new ObjectMapper().readTree(responseBody);
            LOG.info("Parsed " + label + ": " + json);
        } catch (Exception e) {
            LOG.error("Failed to parse " + label, e);
            throw new BusinessException("Failed to parse " + label, e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS – PAYLOAD BUILDER
    // ─────────────────────────────────────────────────────────────────────────

    private IpsRTPPaymentRequestBo buildIpsQrcodeRequest(IpsQrcodeSendPaymentRequestBo requestBo)
            throws BusinessException {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("+02:00"));
        String creationDateTime = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String dateOnly         = now.toLocalDate().toString();

        String traceReference  = requestBo.getTraceReference();
        String amount          = requestBo.getPaymentQrcodeRequestBo().getSum();
        String debtorAccount   = requestBo.getDebtorAccount();
        String creditorAccount = requestBo.getCreditorAccount();
        String documentToken   = requestBo.getDocumentToken();
        String uetr            = requestBo.getUetr();
        String qrexte            = requestBo.getQrExtensionUUID();
        String receiverBic            = requestBo.getReceverBic();
        String e2e             = requestBo.getE2e();

        validateRequired(traceReference, "traceReference");
        validateRequired(documentToken,  "documentToken");
        validateRequired(uetr,           "uetr");
        validateRequired(e2e,            "e2e");

        String xml = buildPacs008Xml(
                traceReference, creationDateTime, dateOnly, amount,
                requestBo.getDebtorName(), debtorAccount,
                requestBo.getCreditorName(), creditorAccount,
                uetr, e2e,receiverBic,qrexte);

        IpsRTPPaymentRequestBo request = new IpsRTPPaymentRequestBo();
        request.setTraceReference(traceReference);
        request.setType("pacs.008.001.10");
        request.setSender(user);
        request.setReceiver(IPS_RECEIVER);
        request.setDocumentToken(documentToken);
        request.setDocument(xml);
        return request;
    }
    
    private String buildPacs008Xml(
            String traceReference, String creationDateTime, String dateOnly,
            String amount, String debtorName, String debtorAccount,
            String creditorName, String creditorAccount,
            String uetr, String e2e, String receiverBic,String qrexte) {

        boolean internal = isInternalBic(receiverBic);

        String toBicTag = internal
                ? "<BICFI>" + receiverBic + "</BICFI>"
                : "<ClrSysMmbId><MmbId>" + receiverBic + "</MmbId></ClrSysMmbId>";

        return new StringBuilder()
            .append("<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">")
            .append("<Body>")
                .append("<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">")
                    .append("<Fr><FIId><FinInstnId><BICFI>").append(OUR_BIC_SHORT).append("</BICFI></FinInstnId></FIId></Fr>")
                    .append("<To><FIId><FinInstnId>").append("<BICFI>BRBUBIBI</BICFI>").append("</FinInstnId></FIId></To>")
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
                                .append("<EndToEndId>").append(e2e).append("</EndToEndId>")
                                .append("<TxId>").append(traceReference).append("</TxId>")
                                .append("<UETR>").append(qrexte).append("</UETR>")
                            .append("</PmtId>")
                            .append("<PmtTpInf>")
                                .append("<ClrChanl>RTNS</ClrChanl>")
                                .append("<LclInstrm><Prtry>PMNI</Prtry></LclInstrm>")
                            .append("</PmtTpInf>")
                            .append("<IntrBkSttlmAmt Ccy=\"BIF\">").append(amount).append("</IntrBkSttlmAmt>")
                            .append("<IntrBkSttlmDt>").append(dateOnly).append("</IntrBkSttlmDt>")
                            .append("<ChrgBr>SLEV</ChrgBr>")
                            .append("<InstgAgt><FinInstnId><BICFI>").append(OUR_BIC_SHORT).append("</BICFI></FinInstnId></InstgAgt>")
                            .append("<InstdAgt><FinInstnId>").append(toBicTag).append("</FinInstnId></InstdAgt>")
                            .append("<Dbtr><Nm>").append(debtorName).append("</Nm></Dbtr>")
                            .append("<DbtrAcct><Id><Othr><Id>").append(debtorAccount).append("</Id></Othr></Id></DbtrAcct>")
                            .append("<DbtrAgt><FinInstnId><BICFI>").append(OUR_BIC_SHORT).append("</BICFI></FinInstnId></DbtrAgt>")
                            .append("<CdtrAgt><FinInstnId>").append(toBicTag).append("</FinInstnId></CdtrAgt>")
                            .append("<Cdtr><Nm>").append(creditorName).append("</Nm></Cdtr>")
                            .append("<CdtrAcct><Id><Othr><Id>").append(creditorAccount).append("</Id></Othr></Id></CdtrAcct>")
                            .append("<Purp><Prtry>007</Prtry></Purp>")
                            .append("<RmtInf>")
                                .append("<Ustrd>Informations de la remise</Ustrd>")
                                .append("<Strd><RfrdDocInf><Tp><CdOrPrtry><Prtry>RQRR</Prtry></CdOrPrtry></Tp></RfrdDocInf></Strd>")
                            .append("</RmtInf>")
                        .append("</CdtTrfTxInf>")
                    .append("</FIToFICstmrCdtTrf>")
                .append("</Document>")
            .append("</Body>")
            .append("</DataPDU>")
            .toString();
    }

    private void validateRequired(String value, String fieldName) throws BusinessException {
        if (StringUtil.isNullOrEmpty(value)) {
            throw new BusinessException(fieldName + " is required");
        }
    }

    private boolean isInternalBic(String receiverBic) {
        return BrbBicRepository.isValidBic(receiverBic);
    }

	
    
}