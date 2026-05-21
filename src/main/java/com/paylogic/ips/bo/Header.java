package com.paylogic.ips.bo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Header {
        private String qrType;
        private String amountType;
        private String pmtContext;
        private int isoVer;
        private String currency;

        public String getQrType() {
            return qrType;
        }

        public void setQrType(String qrType) {
            this.qrType = qrType;
        }

        public String getAmountType() {
            return amountType;
        }

        public void setAmountType(String amountType) {
            this.amountType = amountType;
        }

        public String getPmtContext() {
            return pmtContext;
        }

        public void setPmtContext(String pmtContext) {
            this.pmtContext = pmtContext;
        }

        public int getIsoVer() {
            return isoVer;
        }

        public void setIsoVer(int isoVer) {
            this.isoVer = isoVer;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

		@Override
		public String toString() {
			return "Header [qrType=" + qrType + ", amountType=" + amountType + ", pmtContext=" + pmtContext
					+ ", isoVer=" + isoVer + ", currency=" + currency + "]";
		}
        
        
    }