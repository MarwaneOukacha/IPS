package com.paylogic.ips.prosessor;

import org.springframework.stereotype.Component;

import com.gms.utils.common.StringUtil;
import com.gms.utils.exception.BusinessException;
import com.paylogic.ama.core.model.MemberEndPoint;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ama.endpoint.processor.SimpleWebProcessor;
import com.paylogic.ips.bo.CustomerAccountSearchBo;


@Component
public class CasResolveProcessor extends SimpleWebProcessor<Payment, String, CustomerAccountSearchBo, CustomerAccountSearchBo>{
	
	
	@Override
	protected String convertRequest(Payment incoming, MemberEndPoint destinationEndPoint) {
		// TODO Auto-generated method stub
		destinationEndPoint.setUrl(destinationEndPoint.getUrl()+incoming.getReceiverMobile());
		return null;
	}

	@Override
	protected void validateRequest(Payment incoming, MemberEndPoint destinationEndPoint) throws BusinessException {
		// TODO Auto-generated method stub
		if(StringUtil.isNullOrEmpty(incoming.getReceiverMobile())) {
			throw new BusinessException("receiver mobile number is mandatory");
		}
		
	}

	@Override
	protected void validateResponse(CustomerAccountSearchBo response, MemberEndPoint destinationEndPoint)
			throws BusinessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected CustomerAccountSearchBo convertResponse(CustomerAccountSearchBo response, Payment initialRequest,
			MemberEndPoint destinationEndPoint) throws BusinessException {
		return response;
	}

	@Override
	protected Class<CustomerAccountSearchBo> initP2Class() {
		return CustomerAccountSearchBo.class;
	}

}
