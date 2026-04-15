package com.paylogic.ips.services;


import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gms.security.jwt.JwsHelper;
import com.gms.security.jwt.model.JwsObject;
import com.gms.security.jwt.model.JwtHeader;
import com.gms.utils.common.StringUtil;
import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.WebInterface;
import com.gms.utils.net.webinterface.WebRequest;
import com.paylogic.ips.bo.AccessTokenResponse;

@Service
public class TokenService {

    private static final Logger LOG = Logger.getLogger(TokenService.class);

    @Value("${crtPath}")
    private String crtPath;

    @Value("${casAuthUrl}")
    private String casAuthUrl;

    @Value("${casAuth.url.acceptType:application/json}")
    private String acceptType;

    @Value("${casAuth.url.mediaType:application/x-www-form-urlencoded}")
    private String mediaTypeForm;

    @Value("${casAuth.url.connectTimeout:10000}")
    private int connectTimeout;

    @Value("${casAuth.url.readTimeout:10000}")
    private int readTimeout;

    @Value("${user}")
    private String user;

    @Value("${password}")
    private String password;

    @Value("${senderBic}")
    private String senderBic;

    /** =========================================================================
     *   GET ACCESS TOKEN (used by CAS + OutgoingIPS if needed)
     *  ========================================================================= */
    public String getAccessToken() throws BusinessException {

        WebRequest request = buildTokenRequest();

        try {
            WebInterface.processRequest(request);
        } catch (Exception e) {
            LOG.error("Token request failed", e);
            throw new BusinessException("Token request failed", e);
        }

        if (request.getResponse().getResponseCode() != 200) {
            throw new BusinessException("Error getting access token: " + request.getResponse().getResponseMsg());
        }

        AccessTokenResponse response = request.convertFromJson(
                request.getResponse().getResponseMsg(),
                AccessTokenResponse.class
        );

        return response.getAccessToken();
    }

    /** =========================================================================
     *   GENERATE CLIENT JWT (shared by all services)
     *  ========================================================================= */
    public String generateClientToken() throws BusinessException {
        try {
            X509Certificate cert = loadCertificate();

            JwtHeader header = new JwtHeader();
            header.setAlg(JwsHelper.RS256);
            header.setTyp("JWT");

            Map<String, Object> payload = buildJwtPayload(cert);

            JwsObject jws = new JwsObject(header);
            jws.setPayload(new ObjectMapper().writeValueAsString(payload));

            String publicKey = StringUtil.formatData(
                    cert.getPublicKey().getEncoded(),
                    StringUtil.DataType.BASE64_FORMAT,
                    StringUtil.DEFAULT_ENCODING
            ).replaceAll("\\s+", "");

            JwsHelper.getInstance().sign(jws, publicKey);

            return JwsHelper.getInstance().encodeObject(jws);

        } catch (Exception e) {
            LOG.error("JWT generation failed", e);
            throw new BusinessException("JWT generation failed", e);
        }
    }

    /* ========================================================================== */
    /* PRIVATE METHODS */
    /* ========================================================================== */

    private WebRequest buildTokenRequest() throws BusinessException {
        WebRequest request = new WebRequest();
        request.setUrl(casAuthUrl);
        request.setAcceptType(acceptType);
        request.setMediaType(mediaTypeForm);
        request.setQueryMethod(WebRequest.QUERY_METHOD.POST);
        request.setReadTimeout(readTimeout);
        request.setConnectTimeout(connectTimeout);
        request.setHeader(buildBearerHeader(generateClientToken()));
        request.setBody("grant_type=password&username=" + user + "&password=" + password);
        return request;
    }
    
    public Map<String, String> buildBearerHeader(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        headers.put("Participant-Code", senderBic);
        headers.put("X-Request-ID", UUID.randomUUID().toString());
        return headers;
    }
    private X509Certificate loadCertificate() throws Exception {
        try (FileInputStream fis = new FileInputStream(crtPath)) {
            return (X509Certificate) CertificateFactory
                    .getInstance("X.509")
                    .generateCertificate(fis);
        }
    }
    
    

    private Map<String, Object> buildJwtPayload(X509Certificate cert) {
    	long iat = Instant.now().minusSeconds(600).getEpochSecond(); 
        long exp = iat + 600;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("asrv_type", "client");
        payload.put("asrv_cert_iss", cert.getIssuerX500Principal().getName());
        payload.put("asrv_cert_sn", cert.getSerialNumber().toString(16));
        payload.put("iss", "BKGFBIBIAXXX");
        payload.put("iat", iat);
        payload.put("exp", exp);
        return payload;
    }
}
