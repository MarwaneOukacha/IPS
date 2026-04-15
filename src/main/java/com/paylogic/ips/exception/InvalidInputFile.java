package com.paylogic.ips.exception;

import com.gms.utils.exception.BusinessException;

public class InvalidInputFile extends BusinessException{

	private static final long serialVersionUID = -1488066909922749521L;
	
	public InvalidInputFile() {
		super();
	}
	
	public InvalidInputFile(String message) {
        super(message);
    }

    public InvalidInputFile(Throwable cause) {
        super(cause);
    }

    public InvalidInputFile(String message, Throwable cause) {
        super(message, cause);
    }
}
