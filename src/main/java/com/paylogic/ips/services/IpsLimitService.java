package com.paylogic.ips.services;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.WebInterface;
import com.gms.utils.net.webinterface.WebRequest;
import com.gms.utils.net.webinterface.WebRequest.QUERY_METHOD;
import com.paylogic.ips.bo.IpsSendPaymentRequestBo;

@Service
public class IpsLimitService {

    private static final Logger LOG = Logger.getLogger(IpsLimitService.class);

    @Autowired
    private TokenService tokenService;

    @Value("${ips.limit.input.url}")
    private String ipsInputUrl;

    @Value("${ips.limit.output.url}")
    private String ipsOutputUrl;

    @Value("${walletcore.url.readTimeout:10000}")
    private int readTimeout;

    @Value("${walletcore.url.connectTimeout:10000}")
    private int connectTimeout;
    @Value("${user}")
    private String user;
    
    @Value("${senderBic}")
    private String senderBic;

    // ============================================================
    // COMMON HELPERS
    // ============================================================

    private String currentDate() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
    }

    private String generateMsgId() {
        return "T" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + (int)(Math.random() * 99999);
    }

    // ============================================================
    // 1. CAMT.009 (GET LIMIT)
    // ============================================================

    public void sendCamt009(String accountId) {

        try {
            String msgId = generateMsgId();
            String date = currentDate();

            String xml =
                    "<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">" +
                    "<Body>" +
                    "<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">" +
                    "<Fr><FIId><FinInstnId><BICFI>" + senderBic + "</BICFI></FinInstnId></FIId></Fr>" +
                    "<To><FIId><FinInstnId><BICFI>BRBUBIBI</BICFI></FinInstnId></FIId></To>" +
                    "<BizMsgIdr>" + msgId + "</BizMsgIdr>" +
                    "<MsgDefIdr>camt.009.001.07</MsgDefIdr>" +
                    "<BizSvc>brb.ips.01</BizSvc>" +
                    "<CreDt>" + date + "+02:00</CreDt>" +
                    "</AppHdr>" +
                    "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.009.001.07\">" +
                    "<GetLmt>" +
                    "<MsgHdr><MsgId>" + msgId + "</MsgId><CreDtTm>" + date + "+02:00</CreDtTm></MsgHdr>" +
                    "<LmtQryDef><LmtCrit><NewCrit><SchCrit>" +
                    "<AcctId><Othr><Id>" + accountId + "</Id></Othr></AcctId>" +
                    "<LmtCcy>BIF</LmtCcy>" +
                    "</SchCrit></NewCrit></LmtCrit></LmtQryDef>" +
                    "</GetLmt>" +
                    "</Document>" +
                    "</Body>" +
                    "</DataPDU>";

            IpsSendPaymentRequestBo request = new IpsSendPaymentRequestBo();
            request.setTraceReference(msgId);
            request.setType("camt.009.001.07");
            request.setSender(user);
            request.setReceiver("BRBUBIBAXIPS");
            request.setDocument(xml);

            callIps(request);

        } catch (Exception e) {
            LOG.error("Error sending camt.009", e);
        }
    }

    // ============================================================
    // 2. CAMT.011 (MODIFY LIMIT)
    // ============================================================

    public void sendCamt011(String accountId, String amount) {
    	

        try {
            String msgId = generateMsgId();
            String date = currentDate();

            String xml =
                    "<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">" +
                    "<Body>" +
                    "<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">" +
                    "<Fr><FIId><FinInstnId><BICFI>" + senderBic + "</BICFI></FinInstnId></FIId></Fr>" +
                    "<To><FIId><FinInstnId><BICFI>BRBUBIBI</BICFI></FinInstnId></FIId></To>" +
                    "<BizMsgIdr>" + msgId + "</BizMsgIdr>" +
                    "<MsgDefIdr>camt.011.001.07</MsgDefIdr>" +
                    "<BizSvc>brb.ips.01</BizSvc>" +
                    "<CreDt>" + date + "+02:00</CreDt>" +
                    "</AppHdr>" +
                    "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.011.001.07\">" +
                    "<ModfyLmt>" +
                    "<MsgHdr><MsgId>" + msgId + "</MsgId><CreDtTm>" + date + "</CreDtTm></MsgHdr>" +
                    "<LmtDtls>" +
                    "<LmtId><Cur><Tp><Prtry>IPS</Prtry></Tp>" +
                    "<AcctId><Othr><Id>" + accountId + "</Id></Othr></AcctId>" +
                    "</Cur></LmtId>" +
                    "<NewLmtValSet><Amt><AmtWthCcy Ccy=\"BIF\">" + amount + "</AmtWthCcy></Amt></NewLmtValSet>" +
                    "</LmtDtls>" +
                    "</ModfyLmt>" +
                    "</Document>" +
                    "</Body>" +
                    "</DataPDU>";

            IpsSendPaymentRequestBo request = new IpsSendPaymentRequestBo();
            request.setTraceReference(msgId);
            request.setType("camt.011.001.07");
            request.setSender(user);
            request.setReceiver("BRBUBIBAXIPS");
            request.setDocument(xml);

            callIps(request);

        } catch (Exception e) {
            LOG.error("Error sending camt.011", e);
        }
    }

    // ============================================================
    // 3. IPS OUTPUT (GET)
    // ============================================================

    public String fetchIpsOutput() {

        try {
            String uuid = UUID.randomUUID().toString();
            String url = ipsOutputUrl + uuid + "?service=ips";

            WebRequest req = new WebRequest();
            req.setUrl(url);
            req.setQueryMethod(QUERY_METHOD.GET);
            req.setAcceptType("application/json");
            req.setReadTimeout(readTimeout);
            req.setConnectTimeout(connectTimeout);
            req.setHeader(tokenService.buildBearerHeader(tokenService.getAccessToken()));

            WebInterface.processRequest(req);

            return req.getResponse().getResponseMsg();

        } catch (Exception e) {
            LOG.error("Error fetching IPS output", e);
        }

        return null;
    }

    // ============================================================
    // COMMON IPS CALL
    // ============================================================

    private void callIps(IpsSendPaymentRequestBo request) throws Exception {

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

        String responseMsg = req.getResponse().getResponseMsg();
        int responseCode = req.getResponse().getResponseCode();
        
        JSONObject jsonResponse = new JSONObject(responseMsg);
        
        if (responseCode!= 200) {
            throw new BusinessException(
            		jsonResponse.optString("description")
            );
        }
        LOG.info("IPS Response: HTTP=" + responseCode + " body=" + responseMsg);
    }
}