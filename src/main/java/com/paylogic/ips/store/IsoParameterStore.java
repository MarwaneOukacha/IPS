package com.paylogic.ips.store;

import java.util.HashMap;
import java.util.Map;

import com.gms.utils.exception.BusinessException;
import com.gms.utils.exception.MandatoryFieldEx;
import com.paylogic.ama.core.model.ParameterCategory;
import com.paylogic.ama.core.model.ParameterDetail;
import com.paylogic.ama.endpoint.store.HandlerParameterStore;

public class IsoParameterStore extends HandlerParameterStore{
	public static final String CURRENCY_MAPPING = "CURRENCY_MAPPING";
	public static final String REVERSE_STATUS_MAPPING = "ISO22_REVERSE_STATUS_MAPPING";
	public static final String ISO22_REJECT_CODE_MAPPING = "ISO22_REJECT_CODE_MAPPING";
	public static final String ISO22_PACS008_KYC_MANDATORY = "ISO22_ADD.PACS008_KYC_MANDATORY";
	public static final String ISO22_PACS003_KYC_MANDATORY = "ISO22_ADD.PACS003_KYC_MANDATORY";
	private Map<String, String> currencyMapping;
	private Map<String, String> statusMapping;
	private Map<String, String> rejectMapping;
	private String defaultRejectCode;
	private String pacs008KycMandatory;
	private String pacs003KycMandatory;

	
	public Map<String, String> getCurrencyMapping() {
		return currencyMapping;
	}

	public void setCurrencyMapping(Map<String, String> currencyMapping) {
		this.currencyMapping = currencyMapping;
	}

	public Map<String, String> getStatusMapping() {
		return statusMapping;
	}

	public void setStatusMapping(Map<String, String> statusMapping) {
		this.statusMapping = statusMapping;
	}

	public Map<String, String> getRejectMapping() {
		return rejectMapping;
	}

	public void setRejectMapping(Map<String, String> rejectMapping) {
		this.rejectMapping = rejectMapping;
	}

	public String getDefaultRejectCode() {
		return defaultRejectCode;
	}

	public void setDefaultRejectCode(String defaultRejectCode) {
		this.defaultRejectCode = defaultRejectCode;
	}

	public String getPacs008KycMandatory() {
		return pacs008KycMandatory;
	}

	public void setPacs008KycMandatory(String pacs008KycMandatory) {
		this.pacs008KycMandatory = pacs008KycMandatory;
	}

	public String getPacs003KycMandatory() {
		return pacs003KycMandatory;
	}

	public void setPacs003KycMandatory(String pacs003KycMandatory) {
		this.pacs003KycMandatory = pacs003KycMandatory;
	}

	@Override
	protected void initFieldMap() {
		super.initFieldMap();
		mapping.put("pacs008KycMandatory", ISO22_PACS008_KYC_MANDATORY);
		mapping.put("pacs003KycMandatory", ISO22_PACS003_KYC_MANDATORY);
	}
	
	@Override
	protected void initMandatoryFields() {
		super.initMandatoryFields();
	}
	
	@Override
	protected void buildStore() throws BusinessException {
		super.buildStore();
		loadCurrencies();
		loadStatus();
		loadRejectCodes();
	}

	private void loadCurrencies() throws MandatoryFieldEx {
		ParameterCategory category = categories.get(CURRENCY_MAPPING);
		if(category == null || category.getDetails() == null || category.getDetails().isEmpty()) {
			throw new MandatoryFieldEx(CURRENCY_MAPPING);
		}
		currencyMapping = new HashMap<>(category.getDetails().size());
		for(ParameterDetail det : category.getDetails()) {
			currencyMapping.put(det.getKey(), det.getValue());
		}
	}
	
	private void loadStatus() throws MandatoryFieldEx {
		ParameterCategory category = categories.get(REVERSE_STATUS_MAPPING);
		if(category == null || category.getDetails() == null || category.getDetails().isEmpty()) {
			throw new MandatoryFieldEx(REVERSE_STATUS_MAPPING);
		}
		statusMapping = new HashMap<>(category.getDetails().size());
		for(ParameterDetail det : category.getDetails()) {
			statusMapping.put(det.getKey(), det.getValue());
		}
	}
	
	private void loadRejectCodes() throws MandatoryFieldEx {
		ParameterCategory category = categories.get(ISO22_REJECT_CODE_MAPPING);
		if(category == null || category.getDetails() == null || category.getDetails().isEmpty()) {
			throw new MandatoryFieldEx(ISO22_REJECT_CODE_MAPPING);
		}
		defaultRejectCode = category.getDefaultValue();
		rejectMapping = new HashMap<>(category.getDetails().size());
		for(ParameterDetail det : category.getDetails()) {
			rejectMapping.put(det.getKey(), det.getValue());
		}
	}

	@Override
	public String toString() {
		return "IsoParameterStore {'currencyMapping':'" + currencyMapping + 
								  "', 'statusMapping':'" + statusMapping +
								  "', 'rejectMapping':'" + rejectMapping +
								  "', 'defaultRejectCode':'" + defaultRejectCode +
								  "', 'pacs008KycMandatory':'" + pacs008KycMandatory +
								  "', 'pacs003KycMandatory':'" + pacs003KycMandatory +
								  "', 'internalSendUrl':'" + getInternalSendUrl() + 
								  "', 'internalUpdateUrl':'" + getInternalUpdateUrl() + 
								  "', 'internalInquiryUrl':'" + getInternalInquiryUrl() + 
								  "', 'internalOauth2Url':'" + getInternalOauth2Url() + 
								  "', 'internalMemberUrl':'" + getInternalMemberUrl() + 
								  "', 'internalMediaType':'" + getInternalMediaType() + 
								 /* "', 'internalAcceptType':'" + getInternalAcceptType()+*/
								  "', 'internalDisconnectUrl':'" + getInternalDisconnectUrl() + 
								  "', 'connectTimeout':'" + getConnectTimeout() + 
								  "', 'readTimeout':'" + getReadTimeout() + 
								  "'}";
	}
	
	
}
