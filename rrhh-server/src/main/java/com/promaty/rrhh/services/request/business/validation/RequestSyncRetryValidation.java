package com.promaty.rrhh.services.request.business.validation;

import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.services.request.handler.RequestHandler;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.shared.CurrentUserProjectAccess;

@Component
public class RequestSyncRetryValidation {

	private static final String VALIDATION_MESSAGE = "La validación falló para uno o más campos.";
	private static final String FIELD_STATUS = "status";

	public void validate(Request request, RequestHandler handler) {
		if (!CurrentUserAuthorities.get().contains(handler.validatePermission())) {
			throw new AccessDeniedException("No tiene el permiso requerido para reintentar la sincronización de este tipo de solicitud.");
		}
		if (!hasProjectAccess(request)) {
			throw new AccessDeniedException("La solicitud pertenece a un centro de costo fuera de su acceso.");
		}
		if (!handler.isInSyncError(request.getEntityId())) {
			throw new BusinessValidationException(VALIDATION_MESSAGE, Map.of(
				FIELD_STATUS, "Solo se puede reintentar la sincronización de un registro con error de sincronización."
			));
		}
	}

	private boolean hasProjectAccess(Request request) {
		return CurrentUserProjectAccess.allowedAllProjects()
			|| CurrentUserProjectAccess.projectIds().contains(request.getProject().getId());
	}
}
