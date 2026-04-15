package com.paylogic.ips.controller;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.gms.utils.exception.BusinessException;

import com.gms.utils.net.webinterface.ErrorStatus;


@RestControllerAdvice
public class GlobalDefaultExceptionHandler {
	private static final Logger LOG = Logger.getLogger(GlobalDefaultExceptionHandler.class.getName());


	@ResponseStatus(code=HttpStatus.BAD_REQUEST)
	@ResponseBody 
	@ExceptionHandler(BusinessException.class)
	public ErrorStatus handleException(Exception ex,Locale local){
		return new ErrorStatus(getExceptionTitle(ex), ex.getMessage());
	}
	


	@ResponseStatus(code=HttpStatus.UNAUTHORIZED)
	@ResponseBody 
	@ExceptionHandler({BadCredentialsException.class,LockedException.class,CredentialsExpiredException.class})
	public ErrorStatus handleException(AuthenticationException ex,Locale local){
		return new ErrorStatus(getExceptionTitle(ex), ex.getMessage());
	}

	
	@ResponseStatus(code=HttpStatus.UNAUTHORIZED)
	@ResponseBody 
	@ExceptionHandler({AccessDeniedException.class})
	public ErrorStatus handleException(AccessDeniedException ex,Locale local){
		return new ErrorStatus(getExceptionTitle(ex), ex.getMessage());
	}

	private String getExceptionTitle(Exception ex) {
		if(ex instanceof CredentialsExpiredException) {
			// includes CredentialsExpiredException & CredentialsPukException
			// show the user which kind of Credential Expired occured (Password or Puk)
			return ex.getClass().getSimpleName();
		}else if(ex instanceof AuthenticationException){
			return "unauthorized";
		}else{
			return ex.getClass().getSimpleName();
		}
	}


}
