package com.paylogic.ips.bo;

public class AccountIdBo {
    private String other; // {{AccountNumber}}

	public String getOther() {
		return other;
	}

	public void setOther(String other) {
		this.other = other;
	}

	@Override
	public String toString() {
		return "AccountIdBo [other=" + other + "]";
	}


}
