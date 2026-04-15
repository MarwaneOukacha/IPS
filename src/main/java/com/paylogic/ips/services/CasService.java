package com.paylogic.ips.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gms.utils.common.StringUtil;
import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.WebInterface;
import com.gms.utils.net.webinterface.WebRequest;
import com.paylogic.ama.core.bo.PaymentBo;
import com.paylogic.ama.core.model.AccountInfo;
import com.paylogic.ama.core.model.CustomerKyc;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ips.bo.*;
import com.paylogic.ips.util.CoreUtil;

@Service
public class CasService {

    private static final Logger LOG = Logger.getLogger(CasService.class);

    @Autowired private TokenService tokenService;

    @Value("${casRegisterUrl}")
    private String casRegisterUrl;

    @Value("${casResolveUrl}")
    private String casResolveUrl;

    @Value("${casRemoveUrl}")
    private String casRemoveUrl;

    @Value("${casAuth.url.acceptType:application/json}")
    private String acceptType;

    @Value("${casAuth.url.mediaType:application/json}")
    private String mediaTypeJson;

    @Value("${casAuth.url.connectTimeout:10000}")
    private int connectTimeout;

    @Value("${casAuth.url.readTimeout:10000}")
    private int readTimeout;

    @Value("${senderBic}")
    private String senderBic;

    public CustomerResponseBo registerCustomerToCAS(IpsCustomerIdentityBo request)
            throws BusinessException {

        validateRequest(request);

        String accessToken = tokenService.getAccessToken();

        WebRequest webRequest = buildJsonPostRequest(
                casRegisterUrl,
                request,
                tokenService.buildBearerHeader(accessToken)
        );

        try {
            WebInterface.processRequest(webRequest);
        } catch (IOException e) {
            LOG.error("CAS register request failed", e);
            throw new BusinessException("CAS register request failed", e);
        }

        return webRequest.convertFromJson(
                webRequest.getResponse().getResponseMsg(),
                CustomerResponseBo.class
        );
    }


    public void removeCustomerFromCAS(String customerId) throws BusinessException {

        if (StringUtil.isNullOrEmpty(customerId)) {
            throw new BusinessException("Customer ID is mandatory for removal");
        }

        String accessToken = tokenService.getAccessToken();

        String deleteUrl = casRemoveUrl + "/" + customerId;

        WebRequest webRequest = new WebRequest();
        webRequest.setUrl(deleteUrl);
        webRequest.setAcceptType(acceptType);
        webRequest.setMediaType(mediaTypeJson);
        webRequest.setQueryMethod(WebRequest.QUERY_METHOD.DELETE);
        webRequest.setHeader(tokenService.buildBearerHeader(accessToken));

        try {
            WebInterface.processRequest(webRequest);
        } catch (IOException e) {
            LOG.error("CAS delete request failed", e);
            throw new BusinessException("CAS delete request failed", e);
        }

        if (webRequest.getResponse().getResponseCode() != 200) {
            throw new BusinessException(
                    "Error deleting customer: " + webRequest.getResponse().getResponseMsg()
            );
        }

        LOG.info("Customer removed from CAS successfully: " + customerId);
    }


    public PaymentBo searchCustomerInCAS(PaymentBo payment) throws BusinessException {
		
		 if (payment == null || StringUtil.isNullOrEmpty(payment.getWalletDestination())) {
            throw new BusinessException("Payment or phone number is mandatory for search");
        }
        

        String accessToken = tokenService.getAccessToken();

        String searchUrl = casResolveUrl + payment.getWalletDestination();
        //String searchUrl = casResolveUrl ;

        
        WebRequest webRequest = new WebRequest();
        webRequest.setUrl(searchUrl);
        webRequest.setAcceptType(acceptType);
        webRequest.setMediaType(mediaTypeJson);
        webRequest.setQueryMethod(WebRequest.QUERY_METHOD.GET);
        webRequest.setHeader(tokenService.buildBearerHeader(accessToken));

        try {
        	LOG.info("searchUrl:: "+searchUrl);
            WebInterface.processRequest(webRequest);
        } catch (IOException e) {
            LOG.error("CAS search request failed", e);
            throw new BusinessException("CAS search request failed", e);
        }

        if (webRequest.getResponse().getResponseCode() != 200) {
            throw new BusinessException(
                    "Error searching customer: " + webRequest.getResponse().getResponseMsg()
            );
        }
        
        CustomerAccountSearchBo response = webRequest.convertFromJson(
                webRequest.getResponse().getResponseMsg(),
                CustomerAccountSearchBo.class);
        LOG.info("response from cas:: "+response);
        
        List<AccountInfo>  dstAccounts =new ArrayList();
        
        AccountInfo acc=new AccountInfo();
        acc.setIden(response.getId().getOther());
        acc.setCurrency(response.getCurrency());
        
        Map<String, String> verificationData=new HashMap<String, String>();
        verificationData.put("title", response.getServicer().getBic());
        acc.setVerificationData(verificationData);;
        
        acc.setType("WALLET");
        dstAccounts.add(acc);
        CustomerKyc receiver=new CustomerKyc();
        receiver.setAddress(response.getAddress().getAddress());
        receiver.setCity(response.getAddress().getCity());
        receiver.setCountry(response.getAddress().getCountry());
        receiver.setSurname(response.getSurname());
        receiver.setName(response.getName());
        payment.setReceiverCustomerData(receiver);
        payment.setDstAccounts(dstAccounts);
        return payment;
    }



    private WebRequest buildJsonPostRequest(String url, Object body, Map<String, String> headers) {

        WebRequest request = new WebRequest();
        request.setUrl(url);
        request.setAcceptType(acceptType);
        request.setMediaType(mediaTypeJson);
        request.setQueryMethod(WebRequest.QUERY_METHOD.POST);
        request.setHeader(headers);
        request.setBody(body);
        return request;
    }

    


    protected void validateRequest(IpsCustomerIdentityBo in) throws BusinessException {
        if (in == null) throw new BusinessException("Request must not be null");
        if (in.getUid() == null) throw new BusinessException("UID is mandatory");
        if (StringUtil.isNullOrEmpty(in.getDocumentType()))
            throw new BusinessException("Document type is mandatory");
        if (StringUtil.isNullOrEmpty(in.getDocumentNumber()))
            throw new BusinessException("Document number is mandatory");
        if (StringUtil.isNullOrEmpty(in.getName()))
            throw new BusinessException("Name is mandatory");
        if (StringUtil.isNullOrEmpty(in.getSurname()))
            throw new BusinessException("Surname is mandatory");

        ContactDetailsBo contact = in.getContactDetails();
        if (contact == null || StringUtil.isNullOrEmpty(contact.getMobileNumber())
                || StringUtil.isNullOrEmpty(contact.getEmail())) {
            throw new BusinessException("Valid contact details are mandatory");
        }

        AddressBo address = in.getAddress();
        if (address == null || StringUtil.isNullOrEmpty(address.getCountry())
                || StringUtil.isNullOrEmpty(address.getCity())) {
            throw new BusinessException("Valid address is mandatory");
        }

        if (in.getAliases() == null || in.getAliases().isEmpty()) {
            throw new BusinessException("At least one alias is required");
        }

        for (AliasBo alias : in.getAliases()) {
            if (StringUtil.isNullOrEmpty(alias.getType())
                    || StringUtil.isNullOrEmpty(alias.getValue())) {
                throw new BusinessException("Alias type and value are mandatory");
            }
            if (alias.getAccounts() == null || alias.getAccounts().isEmpty()) {
                throw new BusinessException("Alias must contain accounts");
            }
        }
    }
}
