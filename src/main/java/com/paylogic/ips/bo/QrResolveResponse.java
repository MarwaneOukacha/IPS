package com.paylogic.ips.bo;


public class QrResolveResponse {
    private String merchantName;
    private Double amount;
    private String currency;
    private String uuid;
    private boolean valid;
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	@Override
	public String toString() {
		return "QrResolveResponse [merchantName=" + merchantName + ", amount=" + amount + ", currency=" + currency
				+ ", uuid=" + uuid + ", valid=" + valid + "]";
	}
    
    
}