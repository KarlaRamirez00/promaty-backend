package com.promaty.authorizer.exception;

public class TemporaryPasswordExpiredException extends RuntimeException {

	public TemporaryPasswordExpiredException(String message) {
		super(message);
	}
}
