package com.promaty.user.exception;

public class InvalidResetCodeException extends RuntimeException {

	public InvalidResetCodeException(String message) {
		super(message);
	}
}
