package com.promaty.rrhh.services.request.business.mapper;

import java.util.Map;

import com.promaty.rrhh.entity.RequestAction;
import com.promaty.rrhh.entity.RequestEntityType;

public final class RequestTypeLabelResolver {

	private static final Map<RequestEntityType, Map<RequestAction, String>> ETIQUETAS = Map.of(
		RequestEntityType.CONTRACT, Map.of(
			RequestAction.CREATE, "Nuevo contrato",
			RequestAction.EDIT, "Editar contrato",
			RequestAction.TERMINATE, "Terminar contrato"
		)
	);

	private RequestTypeLabelResolver() {
	}

	public static String resolve(RequestEntityType entityType, RequestAction action) {
		if (entityType == null || action == null) {
			return null;
		}
		return ETIQUETAS.getOrDefault(entityType, Map.of())
			.getOrDefault(action, entityType + " - " + action);
	}
}
