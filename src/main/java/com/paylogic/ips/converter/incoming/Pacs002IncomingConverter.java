package com.paylogic.ips.converter.incoming;

import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gms.utils.exception.BusinessException;
import com.gms.utils.net.webinterface.ErrorStatus;
import com.gms.utils.net.webinterface.WebRequest;
import com.paylogic.ama.core.exception.RemoteEndPointException;
import com.paylogic.ama.core.model.ParameterCategory;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ama.core.utils.BaseCoreUtil.PAYMENT_ACTION;
import com.paylogic.ips.converter.DocumentConverter;
import com.paylogic.ips.iso20022.bo.pacs002.DocumentPacs002;
import com.paylogic.ips.iso20022.bo.pacs002.FIToFIPaymentStatusReportV13;
import com.paylogic.ips.iso20022.bo.pacs002.GroupHeader101;
import com.paylogic.ips.iso20022.bo.pacs002.PaymentTransaction142;
import com.paylogic.ips.iso20022.bo.pacs002.StatusReasonInformation12;
import com.paylogic.ips.iso20022.bo.pacs002.SupplementaryData1;
import com.paylogic.ips.store.IsoParameterStore;
import com.paylogic.ips.util.CoreUtil;

@Component
public class Pacs002IncomingConverter extends DocumentConverter{
	private static final Logger LOG = Logger.getLogger(Pacs002IncomingConverter.class.getName());
	@Autowired
	private IsoParameterStore paramStore;
	
	public Payment convertToPaymentResponse(DocumentPacs002 document,List<ParameterCategory> cats, Payment initialRequest) throws BusinessException {
		Payment payment = new Payment();
		payment.setAction(PAYMENT_ACTION.UPDATE);
		FIToFIPaymentStatusReportV13 pacs002 = document.getFIToFIPmtStsRpt();
		//Header
		convertHeader(pacs002.getGrpHdr(),cats,payment); 
		//Transaction info and status
		PaymentTransaction142 info = pacs002.getTxInfAndSts().get(0);
		convertTransactionInfo(info, cats, payment,initialRequest.getAction());
		//convert supplementary info
		convertSupplementaryData(pacs002.getSplmtryData(),cats,payment);
		//set iso20022 message in additional data
		includeMessage(document,cats,payment,true);
		// set voucher from initial
		payment.setVoucherCode(initialRequest.getVoucherCode());
		return payment;
	}
	
	public Payment convertToUpdatePayment(DocumentPacs002 document,List<ParameterCategory> cats) throws BusinessException {
		Payment payment = new Payment();
		payment.setAction(PAYMENT_ACTION.UPDATE);
		FIToFIPaymentStatusReportV13 pacs002 = document.getFIToFIPmtStsRpt();
		//Header
		convertHeader(pacs002.getGrpHdr(),cats,payment); 
		//Transaction info and status
		PaymentTransaction142 info = pacs002.getTxInfAndSts().get(0);
		//convert supplementary info
		convertSupplementaryData(pacs002.getSplmtryData(),cats,payment);
		//set iso20022 message in additional data
		includeMessage(document,cats,payment,true);
		// set voucher from initial
		payment.setVoucherCode(info.getClrSysRef());
		payment.setState(getState(info, cats));
		return payment;
	}

	private void convertHeader(GroupHeader101 header, List<ParameterCategory> cats, Payment payment) {
		//Acquirer trx ref
		payment.setAcquirerTrxRef(header.getMsgId());
	}
	
	private void convertTransactionInfo(PaymentTransaction142 info, List<ParameterCategory> cats, Payment payment,PAYMENT_ACTION action) throws BusinessException {
		//Payment status
		payment.setState(getState(info,cats));
		//Update time
		if(info.getAccptncDtTm() != null) {
			payment.setUpdateTime(CoreUtil.getDateFromXmlGregorienDate(info.getAccptncDtTm()));
		}
		//Reject msg
		if(!PAYMENT_ACTION.INQUIRY.equals(action) && !CoreUtil.PAY_STATUS_ACCEPTED.equals(payment.getState())) {
			throwErrorException(info);
		}
	}
	
	private void convertSupplementaryData(List<SupplementaryData1> splmtryData, List<ParameterCategory> cats, Payment payment) {
		// Nothing now
	}

	private String getState(PaymentTransaction142 info, List<ParameterCategory> cats) {
		String state = info.getTxSts();
		String paymentStatus = CoreUtil.PAY_STATUS_SUSPECTED;
		for(Entry<String, String> entry : paramStore.getStatusMapping().entrySet()) {
			if(entry.getValue().equals(state)) {
				paymentStatus = entry.getKey();
				break;
			}
		}
		LOG.trace("getState::incoming state is "+state);
		LOG.trace("getState::calculated state is "+paymentStatus);
		return paymentStatus;
	}
	
	private void throwErrorException(PaymentTransaction142 info) throws RemoteEndPointException{
		String error = BusinessException.class.getSimpleName();
		String rejectMsg = "No additional information";
		if(!info.getStsRsnInf().isEmpty()){
			StatusReasonInformation12 statusInfo = info.getStsRsnInf().get(0);
			if(statusInfo.getRsn() != null) {
				rejectMsg = statusInfo.getRsn().getCd()+":";
			}else {
				LOG.info("throwErrorException::no reason given setting default reject code");
			}
			if(statusInfo.getAddtlInf().isEmpty()) {
				rejectMsg += "no Reason";
			}else {
				boolean first = true;
				for(String res : statusInfo.getAddtlInf()) {
					if(first) {
						first = false;
					}else {
						rejectMsg += ",";
					}
					rejectMsg += res;
				}
			}
			if(statusInfo.getRsn() != null) {
				for(Entry<String, String> entry : paramStore.getRejectMapping().entrySet()) {
					if(entry.getValue().equals(statusInfo.getRsn().getCd())) {
						error = entry.getKey();
						break;
					}
				}
			}else {
				error = BusinessException.class.getSimpleName();
			}
		}
		WebRequest request = new WebRequest();
		request.convertToJson(new ErrorStatus(error, rejectMsg));
		throw new RemoteEndPointException(400, request.convertToJson(new ErrorStatus(error, rejectMsg)));
	}

}
