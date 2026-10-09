package com.promaty.rrhh.services.request.business.builder;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.ApprovalDecision;
import com.promaty.rrhh.entity.ApprovalLevel;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.ProjectRepository;

/**
 * RequestValidation ya confirma que el projectId existe antes de llegar acá; el orElseThrow es una
 * defensa ante la carrera entre esa validación y este guardado, no una repetición de esa validación.
 */
@Component
public class RequestRelationsResolver {

	private static final String SUBMODULE_REQUEST = "request";
	private static final String CODE_PENDING_APPROVAL = "PENDING_APPROVAL";
	private static final String CODE_PENDING_VALIDATION = "PENDING_VALIDATION";
	private static final String CODE_APPROVED = "APPROVED";
	private static final String CODE_REJECTED = "REJECTED";

	private final ProjectRepository projectRepository;
	private final PlatformStatusRepository platformStatusRepository;

	public RequestRelationsResolver(
		ProjectRepository projectRepository,
		PlatformStatusRepository platformStatusRepository
	) {
		this.projectRepository = projectRepository;
		this.platformStatusRepository = platformStatusRepository;
	}

	public Project resolveProject(Long projectId) {
		return projectRepository.findById(projectId)
			.orElseThrow(() -> new ResourceNotFoundException("El centro de costo indicado no existe."));
	}

	public PlatformStatus resolveInitialStatus() {
		return platformStatusRepository.findBySubModuleAndCode(SUBMODULE_REQUEST, CODE_PENDING_APPROVAL)
			.orElseThrow(() -> new ResourceNotFoundException("El estado inicial de solicitud no está sembrado."));
	}

	public PlatformStatus resolveNextStatus(ApprovalLevel nivelDecidido, ApprovalDecision decision) {
		String codigoSiguiente = resolveCodigoSiguiente(nivelDecidido, decision);
		return platformStatusRepository.findBySubModuleAndCode(SUBMODULE_REQUEST, codigoSiguiente)
			.orElseThrow(() -> new ResourceNotFoundException(
				"El estado '" + codigoSiguiente + "' de solicitud no está sembrado."));
	}

	private String resolveCodigoSiguiente(ApprovalLevel nivelDecidido, ApprovalDecision decision) {
		if (decision == ApprovalDecision.REJECTED) {
			return CODE_REJECTED;
		}
		return nivelDecidido == ApprovalLevel.PROJECT_MANAGER ? CODE_PENDING_VALIDATION : CODE_APPROVED;
	}
}
