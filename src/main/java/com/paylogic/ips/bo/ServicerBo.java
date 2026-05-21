package com.paylogic.ips.bo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class ServicerBo {
	@JsonAlias({"memberId", "bic"})
	private String bic;
	
	

	

	public String getBic() {
		return bic;
	}

	public void setBic(String other) {
		this.bic = other;
	}

	@Override
	public String toString() {
		return "ServicerBo [bic=" + bic + "]";
	}

	
	
}
