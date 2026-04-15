package com.paylogic.ips.bo;

public class ContactDetailsBo {
	private String mobileNumber;
    private String email;
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@Override
	public String toString() {
		return "ContactDetailsBo [mobileNumber=" + mobileNumber + ", email=" + email + "]";
	}


}
