package com.paylogic.ips.bo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)

public class CustomerResponseBo {

    private UidBo uid;

    private String documentType;
    private String documentNumber;
    private String documentValidityDate;

    private String name;
    private String surname;
    private String gender;
    private String birthDate;
    private String placeOfBirth;

    private String nationality;
    private Boolean resident;

    private String recordId;
    private String status;

    private AddressBo address;
    private ContactDetailsBo contactDetails;

    private List<AliasBo> aliases;

    private Map<String, LinkBo> _links;

	public UidBo getUid() {
		return uid;
	}

	public void setUid(UidBo uid) {
		this.uid = uid;
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

	public String getDocumentValidityDate() {
		return documentValidityDate;
	}

	public void setDocumentValidityDate(String documentValidityDate) {
		this.documentValidityDate = documentValidityDate;
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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public String getPlaceOfBirth() {
		return placeOfBirth;
	}

	public void setPlaceOfBirth(String placeOfBirth) {
		this.placeOfBirth = placeOfBirth;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public Boolean getResident() {
		return resident;
	}

	public void setResident(Boolean resident) {
		this.resident = resident;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public AddressBo getAddress() {
		return address;
	}

	public void setAddress(AddressBo address) {
		this.address = address;
	}

	public ContactDetailsBo getContactDetails() {
		return contactDetails;
	}

	public void setContactDetails(ContactDetailsBo contactDetails) {
		this.contactDetails = contactDetails;
	}

	public List<AliasBo> getAliases() {
		return aliases;
	}

	public void setAliases(List<AliasBo> aliases) {
		this.aliases = aliases;
	}

	public Map<String, LinkBo> getLinks() {
		return _links;
	}

	public void setLinks(Map<String, LinkBo> links) {
		this._links = links;
	}

	@Override
	public String toString() {
		return "CustomerResponseBo [uid=" + uid + ", documentType=" + documentType + ", documentNumber="
				+ documentNumber + ", documentValidityDate=" + documentValidityDate + ", name=" + name + ", surname="
				+ surname + ", gender=" + gender + ", birthDate=" + birthDate + ", placeOfBirth=" + placeOfBirth
				+ ", nationality=" + nationality + ", resident=" + resident + ", recordId=" + recordId + ", status="
				+ status + ", address=" + address + ", contactDetails=" + contactDetails + ", aliases=" + aliases
				+ ", links=" + _links + "]";
	}

    // getters & setters
    
    
    
    
}
