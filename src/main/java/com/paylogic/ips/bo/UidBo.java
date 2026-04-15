package com.paylogic.ips.bo;

public class UidBo {
	
	private String type;   // MOBILE
    private String value;  // +{{MOBILE}}
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
	@Override
	public String toString() {
		return "UidBo [type=" + type + ", value=" + value + "]";
	}
    
    

}
