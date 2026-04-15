package com.paylogic.ips.controller;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gms.utils.exception.BusinessException;
import com.gms.utils.exception.MandatoryFieldEx;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ama.wm.core.bo.EntityPayment;
import com.paylogic.ips.converter.incoming.Pacs002IncomingConverter;
import com.paylogic.ips.event.PaymentSentEvent;
import com.paylogic.ips.iso20022.bo.BaseDocument;
import com.paylogic.ips.services.OutGoingIpsService;

@RestController
@RequestMapping("/v1/internal")
public class InternalController {
    private static final Logger LOG = Logger.getLogger(InternalController.class.getName());
    @Autowired
    private OutGoingIpsService outgoingipsservice;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostMapping(value = "/forward")
    public Payment forward(@RequestBody EntityPayment payment) throws BusinessException, IOException {
        
        LOG.info("forward::payment " + payment);

        // 1. Send to IPS 
        Payment result = outgoingipsservice.sendPaymentToIPS(payment);

        // 2. Get traceReference 
        String traceReference = result.getIssuerTrxRef(); 

        // 3. Trigger event
        eventPublisher.publishEvent(new PaymentSentEvent(traceReference));

        return result;
    }
    
    
    @GetMapping("/transfers/status/{traceReference}")
    public Boolean getPaymentStatus(@PathVariable String traceReference) throws Exception {
    	LOG.info("forward::check payment status "+traceReference);
        return outgoingipsservice.processStatusFromIPS(traceReference);
    }
    
    @PostMapping("/transfers/ack")
    public ResponseEntity<String> receivePacs002(@RequestBody String pacs002Xml) {
        return outgoingipsservice.receivePacs002(pacs002Xml);
    }

    
}
