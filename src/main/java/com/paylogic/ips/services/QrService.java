package com.paylogic.ips.services;


import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.WebInterface;
import com.gms.utils.net.webinterface.WebRequest;
import com.paylogic.ips.bo.IpsSendPaymentRequestBo;
import com.paylogic.ips.bo.QrPayRequest;
import com.paylogic.ips.bo.QrPayResponse;
import com.paylogic.ips.bo.QrResolveResponse;

@Service
public class QrService {

    private static final Logger LOG = Logger.getLogger(QrService.class);
/*
    @Value("${qrrm.url}")
    private String qrrmUrl;
    
    @Value("${documentToken.url}")
    private String tokenUrl;

    @Autowired
    private TokenService tokenService;
    @Value("${receiver}")
    private String receiverBic;

    @Value("${receiver}")
    private String senderBic;
    @Value("${pacs008DebetorUrl}")
    private String pacs008DebtorUrl;
    /**
     * Extract UUID from QR raw data
     
    public String extractUuid(String qrData) throws BusinessException {

        if (qrData == null || qrData.isEmpty()) {
            throw new BusinessException("QR data cannot be empty");
        }

        LOG.info("Extracting UUID from QR data");

        if (qrData.startsWith("UUID:")) {
            return qrData.substring(5);
        }

        // default : assume it is already an UUID
        return qrData;
    }

    /**
     * Resolve QR Code by calling QRR-M
     
    public QrResolveResponse resolveQr(String uuid) throws BusinessException {

        if (uuid == null || uuid.isEmpty()) {
            throw new BusinessException("UUID is missing");
        }

        LOG.info("Resolving QR with UUID = " + uuid);

        try {
            WebRequest request = new WebRequest();
            request.setUrl(qrrmUrl + uuid);
            request.setQueryMethod(WebRequest.QUERY_METHOD.GET);
            request.setAcceptType("application/json");
            request.setHeader(tokenService.buildBearerHeader(tokenService.getAccessToken()));

            LOG.info("Calling QRR-M : " + request.getUrl());
            WebInterface.processRequest(request);

            int code = request.getResponse().getResponseCode();
            String responseBody = request.getResponse().getResponseMsg();

            if (code != 200) {
                LOG.error("QRR-M returned error " + code + " : " + responseBody);
                throw new BusinessException("Error resolving QR : QRR-M returned " + code);
            }

            LOG.info("Response from QRR-M: " + responseBody);
            QrResolveResponse response=new QrResolveResponse();
            //TODO: NEED TO PUT DATA IN RESPONSE
            return response;

        } catch (Exception ex) {
            LOG.error("Failed to resolve QR", ex);
            throw new BusinessException("Unable to resolve QR", ex);
        }
    }
    /**
     * Pays a QR Code by:
     *  1. getting documentToken from QRR-M
     *  2. generating pacs.008
     *  3. sending pacs.008 to IPS
     
    public QrPayResponse pay(QrPayRequest request) throws BusinessException {

        LOG.info("Starting QR payment for UUID = " + request.getQrUuid());

        // 1️⃣ Get documentToken from QRR-M
        String documentToken = getDocumentToken(request.getQrUuid());

        // 2️⃣ Get QR details (already resolved earlier or resolve now)
        QrResolveResponse qrInfo = resolveQr(request.getQrUuid());

        // 3️⃣ Build pacs.008 including documentToken
        String xml = buildPacs008(request, qrInfo, documentToken);

        // 4️⃣ Send pacs.008 to IPS
        sendToIPS(request.getPayment().getIssuerTrxRef(), xml);

        // 5️⃣ Response to mobile
        QrPayResponse resp = new QrPayResponse();
        resp.setStatus("PENDING");
        resp.setTraceReference(request.getPayment().getIssuerTrxRef());
        resp.setMessage("Payment sent to IPS");

        return resp;
    }

    /**
     * Call QRR-M to get the documentToken
     
    private String getDocumentToken(String uuid) throws BusinessException {
        LOG.info("Requesting documentToken from QRR-M for UUID = " + uuid);

        try {
            WebRequest webRequest = new WebRequest();
            webRequest.setUrl(tokenUrl + uuid);
            webRequest.setQueryMethod(WebRequest.QUERY_METHOD.GET);
            webRequest.setAcceptType("application/json");
            webRequest.setHeader(tokenService.buildBearerHeader(tokenService.getAccessToken()));

            WebInterface.processRequest(webRequest);

            int code = webRequest.getResponse().getResponseCode();
            String body = webRequest.getResponse().getResponseMsg();

            if (code != 200) {
                LOG.error("Failed token request. Code=" + code + ", Body=" + body);
                throw new BusinessException("QRR-M token request failed");
            }

            LOG.info("Token response: " + body);

            // Assuming response is like: {"documentToken":"xxxx"}
            return extractToken(body);

        } catch (Exception e) {
            LOG.error("Exception calling getDocumentToken", e);
            throw new BusinessException("Unable to get documentToken", e);
        }
    }

    private String extractToken(String json) {
        return json.replace("{", "")
                   .replace("}", "")
                   .replace("\"", "")
                   .replace("documentToken:", "")
                   .trim();
    }

    /**
     * Build a pacs.008 including the documentToken
     
    private String buildPacs008(QrPayRequest req, QrResolveResponse qr, String documentToken) {

        LOG.info("Building pacs.008 for QR payment");

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String creationDateTime = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String dateOnly = now.toLocalDate().toString();

  /*      String xml =
            "<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">" +
              "<Body>" +
                "<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">" +
                    "<Fr><FIId><FinInstnId><BICFI>" + senderBic + "</BICFI></FinInstnId></FIId></Fr>" +
                    "<To><FIId><FinInstnId><BICFI>" + receiverBic + "</BICFI></FinInstnId></FIId></To>" +
                    "<BizMsgIdr>" + req.getPayment().getIssuerTrxRef() + "</BizMsgIdr>" +
                    "<MsgDefIdr>pacs.008.001.10</MsgDefIdr>" +
                    "<BizSvc>brb.ips.01</BizSvc>" +
                    "<CreDt>" + creationDateTime + "</CreDt>" +
                    "<Prty>0100</Prty>" +
                "</AppHdr>" +

                "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.10\">" +
                  "<FIToFICstmrCdtTrf>" +

                    "<GrpHdr>" +
                      "<MsgId>" + req.getPayment().getIssuerTrxRef() + "</MsgId>" +
                      "<CreDtTm>" + creationDateTime + "</CreDtTm>" +
                      "<NbOfTxs>1</NbOfTxs>" +
                      "<SttlmInf><SttlmMtd>CLRG</SttlmMtd></SttlmInf>" +
                    "</GrpHdr>" +

                    "<CdtTrfTxInf>" +

                      "<PmtId>" +
                        "<InstrId>" + req.getPayment().getIssuerTrxRef() + "</InstrId>" +
                        "<EndToEndId>" + req.getPayment().getIssuerTrxRef() + "</EndToEndId>" +
                        "<TxId>" + req.getPayment().getIssuerTrxRef() + "</TxId>" +
                      "</PmtId>" +

                      "<IntrBkSttlmAmt Ccy=\"" + qr.getCurrency() + "\">" +
                        qr.getAmount() +
                      "</IntrBkSttlmAmt>" +

                      "<IntrBkSttlmDt>" + dateOnly + "</IntrBkSttlmDt>" +

                      "<CdtrAgt><FinInstnId><BICFI>" + receiverBic + "</BICFI></FinInstnId></CdtrAgt>" +
                      "<DbtrAgt><FinInstnId><BICFI>" + senderBic + "</BICFI></FinInstnId></DbtrAgt>" +

                      "<Cdtr><Nm>" + qr.getMerchantName() + "</Nm></Cdtr>" +
                      "<CdtrAcct><Id><Othr><Id>" + qr.getAccountNumber() + "</Id></Othr></Id></CdtrAcct>" +

                      "<RmtInf>" +
                        "<Ustrd>" + req.getPayment().getDescription() + "</Ustrd>" +
                        "<AddtlInf>" + documentToken + "</AddtlInf>" +            //  INSERT TOKEN HERE  
                      "</RmtInf>" +

                    "</CdtTrfTxInf>" +

                  "</FIToFICstmrCdtTrf>" +
                "</Document>" +

              "</Body>" +
            "</DataPDU>";

        LOG.info("Generated PACS.008 XML");

        return null;
    }

    /**
     * Send pacs.008 to IPS
     
    private void sendToIPS(String traceRef, String xml) throws BusinessException {

        LOG.info("Sending pacs.008 to IPS, traceRef=" + traceRef);

        try {
            // Construire l'objet BO
            IpsSendPaymentRequestBo requestBo = new IpsSendPaymentRequestBo();
            requestBo.setTraceReference(traceRef);
            requestBo.setType("pacs.008.001.10");
            requestBo.setSender(senderBic);
            requestBo.setReceiver(receiverBic);

            

            WebRequest webRequest = new WebRequest();
            webRequest.setUrl(pacs008DebtorUrl + traceRef + "?service=ips");
            webRequest.setQueryMethod(WebRequest.QUERY_METHOD.POST);
            webRequest.setAcceptType("application/json");
            webRequest.setMediaType("application/json");
            webRequest.setHeader(tokenService.buildBearerHeader(tokenService.getAccessToken()));

            // Le BO sera automatiquement sérialisé en JSON par WebInterface si supporte les POJO
            webRequest.setBody(requestBo);

            WebInterface.processRequest(webRequest);

            if (webRequest.getResponse().getResponseCode() != 200) {
                LOG.error("IPS returned error: " + webRequest.getResponse().getResponseMsg());
                throw new BusinessException("Failed to send pacs.008 to IPS");
            }

            LOG.info("IPS accepted pacs.008");

        } catch (Exception e) {
            LOG.error("Exception sending pacs.008 to IPS", e);
            throw new BusinessException("Unable to send pacs.008", e);
        }
    }
*/
}
