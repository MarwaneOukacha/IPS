package com.paylogic.ips.services;


import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

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


    @Value("${ips.signing.keystoreFile}")
    private String keystoreFile;

    @Value("${ips.signing.keystorePass}")
    private String keystorePass;

    @Value("${ips.signing.keyAlias}")
    private String keyAlias;

    private KeyStore keyStore;

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
    
    @PostConstruct
    private void initKeyStore() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(
                Files.newInputStream(Paths.get(keystoreFile)),
                keystorePass.toCharArray()
            );
            this.keyStore = ks;
            LOG.info("TokenService keystore loaded OK");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load keystore in TokenService", e);
        }
    }


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


    public String generateClientToken() throws BusinessException {
        try {
            X509Certificate cert = loadCertificate();

            JwtHeader header = new JwtHeader();
            header.setAlg(JwsHelper.RS256);
            header.setTyp("JWT");

            Map<String, Object> payload = buildJwtPayload(cert);

            JwsObject jws = new JwsObject(header);
            jws.setPayload(new ObjectMapper().writeValueAsString(payload));

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keystorePass.toCharArray());
            String privateKeyBase64 = StringUtil.formatData(
                    privateKey.getEncoded(),
                    StringUtil.DataType.BASE64_FORMAT,
                    StringUtil.DEFAULT_ENCODING
            ).replaceAll("\\s+", "");
            JwsHelper.getInstance().sign(jws, privateKeyBase64);

            return JwsHelper.getInstance().encodeObject(jws);

        } catch (Exception e) {
            LOG.error("JWT generation failed", e);
            throw new BusinessException("JWT generation failed", e);
        }
    }


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
        return (X509Certificate) keyStore.getCertificate(keyAlias);
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
