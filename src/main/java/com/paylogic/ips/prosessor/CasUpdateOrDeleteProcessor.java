package com.paylogic.ips.prosessor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.gms.utils.common.StringUtil;
import com.gms.utils.exception.BusinessException;
import com.paylogic.ama.core.model.MemberEndPoint;
import com.paylogic.ama.endpoint.processor.SimpleWebProcessor;

@Component
public class CasUpdateOrDeleteProcessor extends SimpleWebProcessor<String, String, String, String>{

	@Override
	protected String convertRequest(String incoming, MemberEndPoint destinationEndPoint) {
		destinationEndPoint.setUrl(destinationEndPoint.getUrl()+incoming);
		return null;
	}

	@Override
	protected void validateRequest(String incoming, MemberEndPoint destinationEndPoint) throws BusinessException {
		// TODO Auto-generated method stub
		if(StringUtil.isNullOrEmpty(incoming)) {
			throw new BusinessException("recordId is mandatory");
		}
	}

	@Override
	protected void validateResponse(String response, MemberEndPoint destinationEndPoint) throws BusinessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String convertResponse(String response, String initialRequest, MemberEndPoint destinationEndPoint)
			throws BusinessException {
		// TODO Auto-generated method stub
		return HttpStatus.OK.toString();
	}

	@Override
	protected Class<String> initP2Class() {
		// TODO Auto-generated method stub
		return null;
	}

}
