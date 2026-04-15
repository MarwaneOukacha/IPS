package com.paylogic.ips.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gms.utils.common.StringUtil;
import com.gms.utils.exception.BusinessException;
import com.paylogic.ama.core.bo.PaymentBo;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ips.bo.CustomerAccountSearchBo;
import com.paylogic.ips.bo.CustomerResponseBo;
import com.paylogic.ips.bo.IpsCustomerIdentityBo;
import com.paylogic.ips.bo.CasCustomeraRegisterBo;
import com.paylogic.ips.services.CasService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gms.security.jwt.JweHelper;
import com.gms.security.jwt.JwsHelper;
import com.gms.security.jwt.JwtHelper;
import com.gms.security.jwt.model.JweObject;
import com.gms.security.jwt.model.JwsObject;
import com.gms.security.jwt.model.JwtHeader;
import com.gms.security.jwt.model.JwtObject;

@RestController
@RequestMapping("/v1/cas")
public class CasController {
	private static final Logger LOG = Logger.getLogger(CasController.class.getName());
	
	@Autowired private CasService casService;
	private JweHelper jweHelper;
	
	@PostMapping(value = "/register",produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CustomerResponseBo register(@RequestBody IpsCustomerIdentityBo customerIdentityBo) throws BusinessException,IOException {
        LOG.info("register::customerIdentityBo "+customerIdentityBo);
        return casService.registerCustomerToCAS(customerIdentityBo);
    }
	@DeleteMapping(value = "/remove",produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void removeCustomerFromCAS(@RequestParam(required = true) String recordId) throws BusinessException,IOException {
        LOG.info("remove::customerId  "+recordId);
        casService.removeCustomerFromCAS(recordId);
    }
	
	@PostMapping(value = "/search",produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PaymentBo search(@RequestBody PaymentBo payment) throws BusinessException,IOException {
        LOG.info("search::custumer from cas using payment = "+payment);
        return casService.searchCustomerInCAS(payment);
    }
	
	
	

	

}
