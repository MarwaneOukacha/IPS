package com.paylogic.ips.prosessor;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.gms.utils.common.StringUtil;
import com.gms.utils.exception.BusinessException;
import com.paylogic.ama.core.model.MemberEndPoint;
import com.paylogic.ama.endpoint.processor.SimpleWebProcessor;
import com.paylogic.ips.bo.AccountBo;
import com.paylogic.ips.bo.AddressBo;
import com.paylogic.ips.bo.AliasBo;
import com.paylogic.ips.bo.ContactDetailsBo;
import com.paylogic.ips.bo.CustomerResponseBo;
import com.paylogic.ips.bo.IpsCustomerIdentityBo;
import com.paylogic.ips.bo.CasCustomeraRegisterBo;
@Component
public class CasRegisterProcesssor extends SimpleWebProcessor<IpsCustomerIdentityBo, IpsCustomerIdentityBo, CustomerResponseBo, CustomerResponseBo>{

	@Override
	protected IpsCustomerIdentityBo convertRequest(IpsCustomerIdentityBo incoming, MemberEndPoint destinationEndPoint) {
		/*IpsCustomerIdentityBo customerIdentityBo=new IpsCustomerIdentityBo();
		customerIdentityBo.setAddress(incoming.getAddress());
		customerIdentityBo.setAliases(incoming.getAliases());
		customerIdentityBo.setBirthDate(incoming.getBirthDate());
		customerIdentityBo.setContactDetails(incoming.getContactDetails());
		customerIdentityBo.setDocumentNumber(incoming.getDocumentNumber());
		customerIdentityBo.setDocumentType(incoming.getDocumentType());
		customerIdentityBo.setDocumentValidityDate(incoming.getDocumentValidityDate());
		customerIdentityBo.setGender(incoming.getGender());
		customerIdentityBo.setName(incoming.getName());
		customerIdentityBo.setNationality(incoming.getNationality());
		customerIdentityBo.setPlaceOfBirth(incoming.getPlaceOfBirth());
		customerIdentityBo.setResident(incoming.getResident());
		customerIdentityBo.setSurname(incoming.getSurname());
		customerIdentityBo.setUid(incoming.getUid());*/
		return incoming;
		
	}

	protected void validateRequest(IpsCustomerIdentityBo incoming,MemberEndPoint destinationEndPoint) throws BusinessException {
		if (incoming == null) {
	        throw new BusinessException("Incoming request must not be null");
	    }

	    // ======================
	    // UID
	    // ======================
	    if (incoming.getUid() == null) {
	        throw new BusinessException("UID is mandatory");
	    }

	    // ======================
	    // Identity document
	    // ======================
	    if (StringUtil.isNullOrEmpty(incoming.getDocumentType())) {
	        throw new BusinessException("Document type is mandatory");
	    }

	    if (StringUtil.isNullOrEmpty(incoming.getDocumentNumber())) {
	        throw new BusinessException("Document number is mandatory");
	    }

	    if (incoming.getDocumentValidityDate() != null ) {
	        throw new BusinessException("Document validity date is expired");
	    }

	    // ======================
	    // Personal info
	    // ======================
	    if (StringUtil.isNullOrEmpty(incoming.getName())) {
	        throw new BusinessException("Name is mandatory");
	    }

	    if (StringUtil.isNullOrEmpty(incoming.getSurname())) {
	        throw new BusinessException("Surname is mandatory");
	    }

	    // ======================
	    // Contact details
	    // ======================
	    ContactDetailsBo contact = incoming.getContactDetails();
	    if (contact == null) {
	        throw new BusinessException("Contact details are mandatory");
	    }

	    if (contact.getMobileNumber()!=null) {
	        throw new BusinessException("Mobile number is mandatory");
	    }

	    if (StringUtil.isNullOrEmpty(contact.getEmail())) {
	        throw new BusinessException("Email is mandatory");
	    }

	    // ======================
	    // Address
	    // ======================
	    AddressBo address = incoming.getAddress();
	    if (address == null) {
	        throw new BusinessException("Address is mandatory");
	    }

	    if (StringUtil.isNullOrEmpty(address.getCountry())) {
	        throw new BusinessException("Country is mandatory");
	    }

	    if (StringUtil.isNullOrEmpty(address.getCity())) {
	        throw new BusinessException("City is mandatory");
	    }

	    // ======================
	    // Aliases & Accounts
	    // ======================
	    if (incoming.getAliases() == null || incoming.getAliases().isEmpty()) {
	        throw new BusinessException("At least one alias is required");
	    }

	    for (AliasBo alias : incoming.getAliases()) {

	        if (StringUtil.isNullOrEmpty(alias.getType())) {
	            throw new BusinessException("Alias type is mandatory");
	        }

	        if (StringUtil.isNullOrEmpty(alias.getValue())) {
	            throw new BusinessException("Alias value is mandatory");
	        }

	        if (alias.getAccounts() == null || alias.getAccounts().isEmpty()) {
	            throw new BusinessException("At least one account is required for alias " + alias.getValue());
	        }

	        for (AccountBo account : alias.getAccounts()) {

	            if (account.getId() == null) {
	                throw new BusinessException("Account ID is mandatory");
	            }

	            if (StringUtil.isNullOrEmpty(account.getType())) {
	                throw new BusinessException("Account type is mandatory");
	            }

	            if (StringUtil.isNullOrEmpty(account.getCurrency())) {
	                throw new BusinessException("Account currency is mandatory");
	            }

	            if (account.getServicer() == null) {
	                throw new BusinessException("Account servicer is mandatory");
	            }
	        }
	    }

	    // ======================
	    // Destination endpoint (if needed)
	    // ======================
	    if (destinationEndPoint == null) {
	        throw new BusinessException("Destination endpoint is mandatory");
	    }
		
	}

	@Override
	protected void validateResponse(CustomerResponseBo response, MemberEndPoint destinationEndPoint)
			throws BusinessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected CustomerResponseBo convertResponse(CustomerResponseBo response, IpsCustomerIdentityBo initialRequest,
			MemberEndPoint destinationEndPoint) throws BusinessException {
		return response;
	}

	@Override
	protected Class<CustomerResponseBo> initP2Class() {
		return CustomerResponseBo.class;
	}

	

}
