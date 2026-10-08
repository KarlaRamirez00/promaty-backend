package com.promaty.rrhh.services.request.business.builder;

import org.springframework.stereotype.Component;

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
}
