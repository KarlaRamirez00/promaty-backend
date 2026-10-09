package com.promaty.rrhh.services.request.handler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.exception.ResourceNotFoundException;

@Component
public class RequestHandlerRegistry {

	private final Map<RequestEntityType, RequestHandler> handlers;

	public RequestHandlerRegistry(List<RequestHandler> handlers) {
		this.handlers = handlers.stream()
			.collect(Collectors.toMap(RequestHandler::supports, Function.identity()));
	}

	public RequestHandler get(RequestEntityType entityType) {
		RequestHandler handler = handlers.get(entityType);
		if (handler == null) {
			throw new ResourceNotFoundException("No hay un Handler registrado para " + entityType + ".");
		}
		return handler;
	}
}
