package com.promaty.rrhh.services.request.business.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.request.DecideRequestDto;
import com.promaty.rrhh.entity.ApprovalDecision;
import com.promaty.rrhh.entity.ApprovalLevel;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;

@Component
public class RequestDecisionValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";
	private static final String PERMISO_APROBAR = "contract.approve";
	private static final String PERMISO_VALIDAR = "contract.validate";

	public ApprovalLevel validateDecision(Request request, DecideRequestDto dto) {
		ApprovalLevel nivelEsperado = resolveNivelEsperado(request);
		validarPermisoDelNivel(nivelEsperado);
		validarMotivoDeRechazo(dto);
		return nivelEsperado;
	}

	private ApprovalLevel resolveNivelEsperado(Request request) {
		String statusActual = request.getStatus().getCode();
		return switch (statusActual) {
			case "PENDING_APPROVAL" -> ApprovalLevel.PROJECT_MANAGER;
			case "PENDING_VALIDATION" -> ApprovalLevel.HR;
			default -> throw new BusinessValidationException(
				MENSAJE_VALIDACION,
				Map.of("status", "Esta solicitud ya fue decidida, no admite una nueva decision.")
			);
		};
	}

	private void validarPermisoDelNivel(ApprovalLevel nivelEsperado) {
		String permisoRequerido = nivelEsperado == ApprovalLevel.PROJECT_MANAGER ? PERMISO_APROBAR : PERMISO_VALIDAR;
		Set<String> autoridades = CurrentUserAuthorities.get();
		if (!autoridades.contains(permisoRequerido)) {
			throw new AccessDeniedException("No tiene el permiso requerido para decidir en este nivel.");
		}
	}

	private void validarMotivoDeRechazo(DecideRequestDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();
		if (dto.getDecision() == ApprovalDecision.REJECTED
			&& (dto.getRejectionReason() == null || dto.getRejectionReason().isBlank())) {
			errores.put("rejectionReason", "El motivo de rechazo es obligatorio al rechazar.");
		}
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
