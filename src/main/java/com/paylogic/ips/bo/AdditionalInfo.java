package com.paylogic.ips.bo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class AdditionalInfo {
	
	private String customerType;
    private String taxId;
    private String countryOfResidence;
    private String redirectUrl;
	public String getCustomerType() {
		return customerType;
	}
	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}
	public String getTaxId() {
		return taxId;
	}
	public void setTaxId(String taxId) {
		this.taxId = taxId;
	}
	public String getCountryOfResidence() {
		return countryOfResidence;
	}
	public void setCountryOfResidence(String countryOfResidence) {
		this.countryOfResidence = countryOfResidence;
	}
	public String getRedirectUrl() {
		return redirectUrl;
	}
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	@Override
	public String toString() {
		return "AdditionalInfo [customerType=" + customerType + ", taxId=" + taxId + ", countryOfResidence="
				+ countryOfResidence + ", redirectUrl=" + redirectUrl + "]";
	}
	
	
    
    

}
