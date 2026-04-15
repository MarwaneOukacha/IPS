package com.paylogic.ips.converter;

import java.util.HashSet;
import java.util.List;

import com.gms.utils.net.webinterface.WebRequest;
import com.paylogic.ama.core.model.ParameterCategory;
import com.paylogic.ama.core.model.Payment;
import com.paylogic.ama.core.model.PaymentAdditionalData;
import com.paylogic.ama.core.model.PaymentAdditionalDataCat;
import com.paylogic.ips.iso20022.bo.BaseDocument;
import com.paylogic.ips.util.CoreUtil;

public class DocumentConverter {
	protected static final String ISO20022_MSG_ID = "ISO20022_MSG_ID";
	protected static final String ISO20022_MSG_VERSION = "ISO20022_MSG_VERSION";
	protected static final String ISO20022_MSG_DATA = "ISO20022_MSG_DATA";
	protected static final String ISO22_KYC = "ISO22_KYC";
	protected static final String NAME_COMPOSITION = "NAME_COMPOSITION";
	protected static final String NAME_SEPARATOR = "NAME_SEPARATOR";
	protected static final String DEFAULT_NAME_COMPOSITION = "%f %m %s";
	protected static final String DEFAULT_NAME_SEPARATOR = " ";

	protected void includeMessage(BaseDocument document, List<ParameterCategory> cats, Payment payment,boolean in) {
		if(payment.getAdditionalData() == null) {
			payment.setAdditionalData(new HashSet<>());
		}
		// set iso msg as additional data
		PaymentAdditionalDataCat cat = new PaymentAdditionalDataCat();
		cat.setAdditionalData(new HashSet<>());
		String strIn = in ? "IN" : "OUT";
		cat.setIden(strIn+"@"+document.getDocumentVersion()+"@"+document.getMsgId());
		PaymentAdditionalData data = new PaymentAdditionalData();
		data.setKey(ISO20022_MSG_ID);
		data.setValue(document.getMsgId());
		data.setLoggeable(CoreUtil.YES);
		data.setEncrypted(CoreUtil.NO);
		cat.getAdditionalData().add(data);
		data = new PaymentAdditionalData();
		data.setKey(ISO20022_MSG_VERSION);
		data.setValue(document.getDocumentVersion());
		data.setLoggeable(CoreUtil.YES);
		data.setEncrypted(CoreUtil.NO);
		cat.getAdditionalData().add(data);
		data = new PaymentAdditionalData();
		data.setKey(ISO20022_MSG_DATA);
		WebRequest req = new WebRequest();
		data.setValue(req.convertToXML(document));
		data.setLoggeable(CoreUtil.YES);
		data.setEncrypted(CoreUtil.NO);
		cat.getAdditionalData().add(data);
		payment.getAdditionalData().add(cat);
	}
}
