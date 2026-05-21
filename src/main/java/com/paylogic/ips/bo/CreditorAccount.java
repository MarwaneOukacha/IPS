package com.paylogic.ips.bo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)

public  class CreditorAccount {
        private String other;

        public String getOther() {
            return other;
        }

        public void setOther(String other) {
            this.other = other;
        }

		@Override
		public String toString() {
			return "CreditorAccount [other=" + other + "]";
		}
        
    }

    