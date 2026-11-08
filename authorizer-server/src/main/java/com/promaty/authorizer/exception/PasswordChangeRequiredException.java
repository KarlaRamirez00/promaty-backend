package com.promaty.authorizer.exception;

public class PasswordChangeRequiredException extends RuntimeException {

	public PasswordChangeRequiredException(String message) {
		super(message);
	}
}
