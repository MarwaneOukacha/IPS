package com.paylogic.ips.bo;

public class ServicerBo {

	private String bic;

	public String getBic() {
		return bic;
	}

	public void setBic(String other) {
		this.bic = other;
	}

	@Override
	public String toString() {
		return "ServicerBo [other=" + bic + "]";
	} 
	
}
