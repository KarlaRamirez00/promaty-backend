package com.promaty.rrhh.config;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RestAuthErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public RestAuthErrorHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
			throws IOException {
		escribirError(response, HttpStatus.UNAUTHORIZED,
			"No estas autenticado. Falta el token de acceso o no es valido.");
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
			throws IOException {
		escribirError(response, HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta accion.");
	}

	private void escribirError(HttpServletResponse response, HttpStatus status, String mensaje) throws IOException {
		ErrorResponse error = new ErrorResponse(status.value(), status.name(), mensaje, Map.of());
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), BaseData.error(error));
	}
}
