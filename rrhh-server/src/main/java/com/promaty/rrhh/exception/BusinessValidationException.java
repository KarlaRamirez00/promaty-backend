package com.promaty.rrhh.exception;

import java.util.Map;

public class BusinessValidationException extends RuntimeException {

	private final Map<String, String> errorFields;

	public BusinessValidationException(String message, Map<String, String> errorFields) {
		super(message);
		this.errorFields = errorFields;
	}

	public Map<String, String> getErrorFields() {
		return errorFields;
	}
}
