package com.promaty.rrhh.services.request.handler;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

	/**
	 * Tipos de entidad que el usuario actual puede leer, según readPermission() de cada Handler
	 * registrado — usado para no mostrarle filas de un tipo para el que no tiene permiso de lectura
	 * (GET /requests mezcla todos los tipos en una sola página).
	 */
	public List<RequestEntityType> entityTypesReadableBy(Set<String> authorities) {
		return handlers.values().stream()
			.filter(handler -> authorities.contains(handler.readPermission()))
			.map(RequestHandler::supports)
			.toList();
	}
}
