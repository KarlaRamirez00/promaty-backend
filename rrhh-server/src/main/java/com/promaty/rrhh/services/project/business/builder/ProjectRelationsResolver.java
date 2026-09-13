package com.promaty.rrhh.services.project.business.builder;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.Client;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.ProjectSpecialty;
import com.promaty.rrhh.entity.ProjectType;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ClientRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.ProjectSpecialtyRepository;
import com.promaty.rrhh.repository.ProjectTypeRepository;

/**
 * ProjectValidation ya confirma que las 4 FK existen antes de llegar acá; el orElseThrow es una
 * defensa ante la carrera entre esa validación y este guardado (ej. se borra el registro entremedio),
 * no una repetición de esa validación.
 */
@Component
public class ProjectRelationsResolver {

	private static final String SUB_MODULE_PROJECT = "project";
	private static final String CODIGO_ESTADO_INICIAL = "PLANNED";

	private final ProjectTypeRepository projectTypeRepository;
	private final ProjectSpecialtyRepository projectSpecialtyRepository;
	private final ClientRepository clientRepository;
	private final PlatformStatusRepository platformStatusRepository;

	public ProjectRelationsResolver(
		ProjectTypeRepository projectTypeRepository,
		ProjectSpecialtyRepository projectSpecialtyRepository,
		ClientRepository clientRepository,
		PlatformStatusRepository platformStatusRepository
	) {
		this.projectTypeRepository = projectTypeRepository;
		this.projectSpecialtyRepository = projectSpecialtyRepository;
		this.clientRepository = clientRepository;
		this.platformStatusRepository = platformStatusRepository;
	}

	public ProjectType resolveType(Long typeId) {
		return projectTypeRepository.findById(typeId)
			.orElseThrow(() -> new ResourceNotFoundException("El tipo de proyecto indicado no existe."));
	}

	public ProjectSpecialty resolveSpecialty(Long specialtyId) {
		return projectSpecialtyRepository.findById(specialtyId)
			.orElseThrow(() -> new ResourceNotFoundException("La especialidad indicada no existe."));
	}

	public Client resolveClient(Long clientId) {
		return clientRepository.findById(clientId)
			.orElseThrow(() -> new ResourceNotFoundException("El mandante indicado no existe."));
	}

	public PlatformStatus resolveStatus(Long statusId) {
		return platformStatusRepository.findById(statusId)
			.orElseThrow(() -> new ResourceNotFoundException("El estado indicado no existe."));
	}

	public PlatformStatus resolveDefaultStatus() {
		return platformStatusRepository.findBySubModuleAndCode(SUB_MODULE_PROJECT, CODIGO_ESTADO_INICIAL)
			.orElseThrow(() -> new ResourceNotFoundException("No existe el estado inicial de proyecto (PLANNED)."));
	}
}
