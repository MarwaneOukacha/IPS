package com.paylogic.ips.processor.outgoing;

import org.springframework.stereotype.Component;

import com.gms.utils.exception.BusinessException;
import com.paylogic.ama.core.model.MemberEndPoint;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ama.endpoint.processor.SimpleWebProcessor;
import com.paylogic.ips.bo.IpsSendPaymentRequestBo;
import com.paylogic.ips.bo.IpsSendPaymentResponseBo;

@Component
public class OutgoingIpsProcessor extends SimpleWebProcessor<Payment, IpsSendPaymentRequestBo, IpsSendPaymentResponseBo, Payment>{

	@Override
	protected IpsSendPaymentRequestBo convertRequest(Payment incoming, MemberEndPoint destinationEndPoint) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void validateRequest(Payment incoming, MemberEndPoint destinationEndPoint) throws BusinessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void validateResponse(IpsSendPaymentResponseBo response, MemberEndPoint destinationEndPoint)
			throws BusinessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Payment convertResponse(IpsSendPaymentResponseBo response, Payment initialRequest,
			MemberEndPoint destinationEndPoint) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Class<IpsSendPaymentResponseBo> initP2Class() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
