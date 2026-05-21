package com.paylogic.ips.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gms.utils.exception.BusinessException;
import com.paylogic.ips.bo.IpsRTPSendPaymentRequestBo;
import com.paylogic.ips.event.PaymentSentEvent;
import com.paylogic.ips.services.ServiceRTP;

@RestController
@RequestMapping("/RTP")
public class IpsRTPController {
	
	@Autowired private ServiceRTP service;
	@Autowired
    private ApplicationEventPublisher eventPublisher;
	
	@GetMapping("/get")
    public String getReceivePain013() throws BusinessException
                            {

        return service.receivePain013();
    }

	@PostMapping("/send")
    public void  startPayment(@RequestBody IpsRTPSendPaymentRequestBo ipsRTPSendPaymentRequestBo) throws BusinessException
                            {
	// 	1. Send payment
        service.initiateRTPPayment(ipsRTPSendPaymentRequestBo);
     // 2. Trigger event
        eventPublisher.publishEvent(new PaymentSentEvent(ipsRTPSendPaymentRequestBo.getTraceReference()));
    }
	
	@PostMapping("/send/request")
    public void  startRequest(@RequestBody IpsRTPSendPaymentRequestBo ipsRTPSendPaymentRequestBo) throws BusinessException
                            {
	// 	1. Send payment
        service.initiateRTPRequest(ipsRTPSendPaymentRequestBo);
    }

}
