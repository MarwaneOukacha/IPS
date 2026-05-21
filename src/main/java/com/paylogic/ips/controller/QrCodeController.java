package com.paylogic.ips.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gms.utils.exception.BusinessException;
import com.paylogic.ips.bo.IpsQrcodeSendPaymentRequestBo;
import com.paylogic.ips.bo.QrCodeRequestDto;
import com.paylogic.ips.bo.QrCodeResponseDto;
import com.paylogic.ips.bo.QrPaymentResponseBo;
import com.paylogic.ips.event.PaymentSentEvent;
import com.paylogic.ips.services.ServiceQrcode;


@RestController
@RequestMapping("/qrcode")
public class QrCodeController {
	@Autowired private ServiceQrcode service;
	@Autowired
    private ApplicationEventPublisher eventPublisher;
	
	@GetMapping("/get/{uuid}")
    public QrPaymentResponseBo getQrCodeInfos(@PathVariable("uuid") String uuid) throws BusinessException{
        return service.getQrCodeInfos(uuid);
    }

	@PostMapping("/send")
    public void  startPayment(@RequestBody IpsQrcodeSendPaymentRequestBo ipsQrcodeSendPaymentRequestBo) throws BusinessException
                            {
	// 	1. Send payment
        service.initiateQrcodePayment(ipsQrcodeSendPaymentRequestBo);
     // 2. Trigger event
        eventPublisher.publishEvent(new PaymentSentEvent(ipsQrcodeSendPaymentRequestBo.getTraceReference()));
    }
	
	@PostMapping("/create")
    public QrCodeResponseDto createStiticQrCode(@RequestBody QrCodeRequestDto qrCodeRequestDto) throws BusinessException
                            {
		 return service.createQrCode(qrCodeRequestDto);
    }
	

}
