package com.paylogic.ips.bo;

public class PaymentAmountBo {

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
		return "PaymentAmountBo [sum=" + sum + ", currency=" + currency + "]";
	}
    
    
}