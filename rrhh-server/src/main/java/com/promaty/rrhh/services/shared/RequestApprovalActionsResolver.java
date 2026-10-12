package com.promaty.rrhh.services.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.shared.Action;

/**
 * Deriva las acciones de aprobación/validación disponibles para una fila de Request o de la entidad
 * que esa Request afecta (ej. Contract) — ambas comparten el mismo ciclo PENDING_APPROVAL/
 * PENDING_VALIDATION, la regla vive en un solo lugar para no desalinearse entre los dos listados.
 */
@Component
public class RequestApprovalActionsResolver {

	private static final String CODE_PENDING_APPROVAL = "PENDING_APPROVAL";
	private static final String CODE_PENDING_VALIDATION = "PENDING_VALIDATION";
	private static final String PERMISO_APROBAR = "contract.approve";
	private static final String PERMISO_VALIDAR = "contract.validate";

	public List<Action> resolve(String statusCode, Set<String> authorities) {
		List<Action> actions = new ArrayList<>();
		if (CODE_PENDING_APPROVAL.equals(statusCode) && authorities.contains(PERMISO_APROBAR)) {
			actions.add(Action.APPROVE);
		} else if (CODE_PENDING_VALIDATION.equals(statusCode) && authorities.contains(PERMISO_VALIDAR)) {
			actions.add(Action.VALIDATE);
		}
		return actions;
	}
}
