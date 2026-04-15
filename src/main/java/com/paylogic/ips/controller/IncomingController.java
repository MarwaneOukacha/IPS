package com.paylogic.ips.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gms.utils.net.webinterface.WebInterface;
import com.gms.utils.net.webinterface.WebRequest;
import com.gms.utils.net.webinterface.WebRequest.QUERY_METHOD;

@RestController
public class IncomingController {
	
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
	
	@PostMapping("/test")
	public void test() throws IOException {
		WebRequest tokenRequest = buildTokenRequest();
	    WebInterface.processRequest(tokenRequest);
		System.out.print("testt:: "+tokenRequest.getResponse().getResponseMsg());
		
	}

	
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
}
