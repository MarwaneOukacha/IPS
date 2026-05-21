package com.paylogic.ips.bo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)

public  class Amount {
        private String sum;
        private String currency;

        public String getSum() {
            return sum;
        }

        public void setSum(String sum) {
            this.sum = sum;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

		@Override
		public String toString() {
			return "Amount [sum=" + sum + ", currency=" + currency + "]";
		}
        
    }