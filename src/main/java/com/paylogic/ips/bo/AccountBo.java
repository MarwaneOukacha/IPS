package com.paylogic.ips.bo;

public class AccountBo {
	
	private AccountIdBo id;
    private String type;        // ACCT or WLLT
    private String currency;    // BIF
    private Boolean isDefault;
    private ServicerBo servicer;
	public AccountIdBo getId() {
		return id;
	}
	public void setId(AccountIdBo id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public Boolean getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}
	public ServicerBo getServicer() {
		return servicer;
	}
	public void setServicer(ServicerBo servicer) {
		this.servicer = servicer;
	}
	@Override
	public String toString() {
		return "AccountBo [id=" + id + ", type=" + type + ", currency=" + currency + ", isDefault=" + isDefault
				+ ", servicer=" + servicer + "]";
	}
    

}
