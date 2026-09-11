package com.promaty.authorizer.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.promaty.authorizer.dto.response.BaseData;
import com.promaty.authorizer.dto.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<BaseData<Void>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> errorFields = new LinkedHashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errorFields.put(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return buildResponse(HttpStatus.BAD_REQUEST, mensajeConCampos(errorFields), errorFields);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<BaseData<Void>> handleNotFound(ResourceNotFoundException ex) {
		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<BaseData<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
		return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), Map.of());
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<BaseData<Void>> handleDuplicate(DuplicateResourceException ex) {
		return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
	}

	@ExceptionHandler(BusinessValidationException.class)
	public ResponseEntity<BaseData<Void>> handleBusinessValidation(BusinessValidationException ex) {
		return buildResponse(HttpStatus.BAD_REQUEST, mensajeConCampos(ex.getErrorFields()), ex.getErrorFields());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<BaseData<Void>> handleUnexpected(Exception ex) {
		log.error("Error inesperado no mapeado", ex);
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado.", Map.of());
	}

	private ResponseEntity<BaseData<Void>> buildResponse(HttpStatus status, String message, Map<String, String> errorFields) {
		ErrorResponse error = new ErrorResponse(status.value(), status.name(), message, errorFields);
		return ResponseEntity.status(status).body(BaseData.error(error));
	}

	private String mensajeConCampos(Map<String, String> errorFields) {
		String campos = String.join(", ", errorFields.keySet());
		return "Revisa los siguientes campos: " + campos + ".";
	}
}
