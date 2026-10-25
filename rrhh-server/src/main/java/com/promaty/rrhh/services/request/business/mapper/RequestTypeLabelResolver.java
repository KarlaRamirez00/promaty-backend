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
		),
		RequestEntityType.TERMINATION, Map.of(
			RequestAction.CREATE, "Nuevo finiquito"
		)
	);

	// Fallback si entityType/action no calzan en ETIQUETAS (ej. una combinación nueva aún sin
	// etiqueta propia) — nunca debe filtrar el nombre del enum a la pantalla del usuario final.
	private static final Map<RequestEntityType, String> NOMBRE_GENERICO = Map.of(
		RequestEntityType.CONTRACT, "Contrato",
		RequestEntityType.TERMINATION, "Finiquito"
	);
	private static final String NOMBRE_GENERICO_DESCONOCIDO = "Solicitud";

	private RequestTypeLabelResolver() {
	}

	public static String resolve(RequestEntityType entityType, RequestAction action) {
		if (entityType == null || action == null) {
			return null;
		}
		return ETIQUETAS.getOrDefault(entityType, Map.of())
			.getOrDefault(action, NOMBRE_GENERICO.getOrDefault(entityType, NOMBRE_GENERICO_DESCONOCIDO));
	}
}
