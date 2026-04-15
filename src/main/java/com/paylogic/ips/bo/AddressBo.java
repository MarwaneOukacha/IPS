package com.paylogic.ips.bo;

public class AddressBo {
	private String country;
    private String city;
    private String stateProvinceRegion;
    private String address;
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getStateProvinceRegion() {
		return stateProvinceRegion;
	}
	public void setStateProvinceRegion(String stateProvinceRegion) {
		this.stateProvinceRegion = stateProvinceRegion;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	@Override
	public String toString() {
		return "AddressBo [country=" + country + ", city=" + city + ", stateProvinceRegion=" + stateProvinceRegion
				+ ", address=" + address + "]";
	}
    
    

}
