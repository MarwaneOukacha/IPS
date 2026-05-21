package com.paylogic.ips.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    	ServicerBo servicer=new ServicerBo();
    	servicer.setBic(senderBic);
    	request.getAliases().get(0).getAccounts().get(0).setServicer(servicer);
    	LOG.info("register:: "+request);
    	
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

        String responseMsg = webRequest.getResponse().getResponseMsg();
        int responseCode = webRequest.getResponse().getResponseCode();
        
        JSONObject jsonResponse = new JSONObject(responseMsg);
        
        if (responseCode!= 200) {
            throw new BusinessException(
            		jsonResponse.optString("description")
            );
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseMsg);

            CustomerResponseBo response = new CustomerResponseBo();

            // --- UID object ---
            if (root.has("uid")) {
                JsonNode uidNode = root.get("uid");

                UidBo uid = new UidBo();
                uid.setType(getText(uidNode, "type"));   // MOBILE
                uid.setValue(getText(uidNode, "value"));

                response.setUid(uid);
            }

            // --- simple fields ---
            response.setName(getText(root, "name"));
            response.setSurname(getText(root, "surname"));
            response.setGender(getText(root, "gender"));
            response.setStatus(getText(root, "status"));
            response.setNationality(getText(root, "nationality"));
            response.setDocumentType(getText(root, "documentType"));
            response.setDocumentNumber(getText(root, "documentNumber"));
            response.setRecordId((getText(root, "recordId")));
            
            


            // --- dates ---
            if (root.hasNonNull("documentValidityDate")) {
                response.setDocumentValidityDate(getText(root, "documentValidityDate"));
            }

            if (root.hasNonNull("birthDate")) {
                response.setBirthDate(getText(root, "birthDate"));
            }

            // --- address ---
            if (root.has("address")) {
                JsonNode addressNode = root.get("address");

                AddressBo address = new AddressBo();
                address.setCity(getText(addressNode, "city"));
                address.setCountry(getText(addressNode, "country"));
                address.setAddress(getText(addressNode, "address"));

                response.setAddress(address);
            }

            // --- contact details ---
            if (root.has("contactDetails")) {
                JsonNode contactNode = root.get("contactDetails");

                ContactDetailsBo contact = new ContactDetailsBo();
                contact.setMobileNumber(getText(contactNode, "mobileNumber"));
                contact.setEmail(getText(contactNode, "email"));

                response.setContactDetails(contact);
            }

            return response;

        } catch (Exception e) {
            LOG.error("Failed to manually map CAS response", e);
            throw new BusinessException("Invalid CAS response format", e);
        }
    }

    private String getText(JsonNode node, String field) {
        return (node != null && node.hasNonNull(field))
                ? node.get(field).asText()
                : null;
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
        
        String responseMsg = webRequest.getResponse().getResponseMsg();
        int responseCode = webRequest.getResponse().getResponseCode();
        
        JSONObject jsonResponse = new JSONObject(responseMsg);
        
        if (responseCode!= 200) {
            throw new BusinessException(
            		jsonResponse.optString("description")
            );
        }



        LOG.info("Customer removed from CAS successfully: " + customerId);
    }


    public PaymentBo searchCustomerInCAS(PaymentBo payment) throws BusinessException, UnsupportedEncodingException {
		
		 if (payment == null || StringUtil.isNullOrEmpty(payment.getWalletDestination())) {
            throw new BusinessException("Payment or phone number is mandatory for search");
        }
        

        String accessToken = tokenService.getAccessToken();

        String phone = payment.getWalletDestination();

        if (!phone.startsWith("+")) {
            phone = "+" + phone;
        }

        Map<String, String> params = new HashMap<>();
        params.put("aliasType", "MOBILE");
        String encodedPhone = URLEncoder.encode(phone, "UTF-8");

        params.put("aliasValue", encodedPhone); // RAW +257...

        LOG.info("encodedPhone:: "+encodedPhone);

        
        WebRequest webRequest = new WebRequest();
        webRequest.setUrl(casResolveUrl);
        webRequest.setAcceptType(acceptType);
        webRequest.setMediaType(mediaTypeJson);
        webRequest.setParams(params);
        webRequest.setEncodeParams(false);
        webRequest.setQueryMethod(WebRequest.QUERY_METHOD.GET);
        webRequest.setHeader(tokenService.buildBearerHeader(accessToken));

        try {
        	LOG.info("searchUrl:: "+casResolveUrl);
            WebInterface.processRequest(webRequest);
        } catch (IOException e) {
            LOG.error("CAS search request failed", e);
            throw new BusinessException("CAS search request failed", e);
        }

        String responseMsg = webRequest.getResponse().getResponseMsg();
        int responseCode = webRequest.getResponse().getResponseCode();
        
        JSONObject jsonResponse = new JSONObject(responseMsg);
        
        if (responseCode!= 200) {
            throw new BusinessException(
            		jsonResponse.optString("description")
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
        
        acc.setType(response.getType());
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
