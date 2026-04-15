package com.paylogic.ips.bo;

public class CustomerAccountSearchBo {
	private AccountIdBo id;

    private String type;        // WLLT
    private String currency;    // BIF

    private ServicerBo servicer;

    private String name;
    private String surname;

    private Boolean isDefault;

    private AddressBo address;

    private String documentType;
    private String documentNumber;
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
	public ServicerBo getServicer() {
		return servicer;
	}
	public void setServicer(ServicerBo servicer) {
		this.servicer = servicer;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public Boolean getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}
	public AddressBo getAddress() {
		return address;
	}
	public void setAddress(AddressBo address) {
		this.address = address;
	}
	public String getDocumentType() {
		return documentType;
	}
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}
	public String getDocumentNumber() {
		return documentNumber;
	}
	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}
	@Override
	public String toString() {
		return "CustomerAccountSearchBo [id=" + id + ", type=" + type + ", currency=" + currency + ", servicer="
				+ servicer + ", name=" + name + ", surname=" + surname + ", isDefault=" + isDefault + ", address="
				+ address + ", documentType=" + documentType + ", documentNumber=" + documentNumber + "]";
	}
    
    

}
