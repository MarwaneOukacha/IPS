package com.paylogic.ips.bo;

import java.util.List;

public class AliasBo {
	
	private String type;   // MOBILE
    private String value;  // +{{MOBILE}}
    private List<AccountBo> accounts;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public List<AccountBo> getAccounts() {
		return accounts;
	}
	public void setAccounts(List<AccountBo> accounts) {
		this.accounts = accounts;
	}
	@Override
	public String toString() {
		return "AliasBo [type=" + type + ", value=" + value + ", accounts=" + accounts + "]";
	}
	
    
}
