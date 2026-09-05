package com.promaty.rrhh.services.project.business.validation;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.project.CreateProjectDto;
import com.promaty.rrhh.dto.project.UpdateProjectDto;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.ClientRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.repository.ProjectSpecialtyRepository;
import com.promaty.rrhh.repository.ProjectTypeRepository;

@Component
public class ProjectValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";
	private static final String CENTRO_COSTO_DUPLICADO = "Ya existe un proyecto con este centro de costo.";

	private final ProjectRepository projectRepository;
	private final ProjectTypeRepository projectTypeRepository;
	private final ProjectSpecialtyRepository projectSpecialtyRepository;
	private final ClientRepository clientRepository;
	private final PlatformStatusRepository platformStatusRepository;

	public ProjectValidation(
		ProjectRepository projectRepository,
		ProjectTypeRepository projectTypeRepository,
		ProjectSpecialtyRepository projectSpecialtyRepository,
		ClientRepository clientRepository,
		PlatformStatusRepository platformStatusRepository
	) {
		this.projectRepository = projectRepository;
		this.projectTypeRepository = projectTypeRepository;
		this.projectSpecialtyRepository = projectSpecialtyRepository;
		this.clientRepository = clientRepository;
		this.platformStatusRepository = platformStatusRepository;
	}

	public void validateCreate(CreateProjectDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		if (projectRepository.findByCostCenterCode(dto.getCostCenterCode()).isPresent()) {
			errores.put("costCenterCode", CENTRO_COSTO_DUPLICADO);
		}
		validarRelaciones(errores, dto.getTypeId(), dto.getSpecialtyId(), dto.getClientId(), dto.getStatusId());
		validarRangoFechas(errores, dto.getStartDate(), dto.getEndDate());

		lanzarSiHayErrores(errores);
	}

	public void validateUpdate(Long id, UpdateProjectDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		Optional<Project> conMismoCentroCosto = projectRepository.findByCostCenterCode(dto.getCostCenterCode());
		if (conMismoCentroCosto.isPresent() && !conMismoCentroCosto.get().getId().equals(id)) {
			errores.put("costCenterCode", CENTRO_COSTO_DUPLICADO);
		}
		validarRelaciones(errores, dto.getTypeId(), dto.getSpecialtyId(), dto.getClientId(), dto.getStatusId());
		validarRangoFechas(errores, dto.getStartDate(), dto.getEndDate());

		lanzarSiHayErrores(errores);
	}

	private void validarRelaciones(Map<String, String> errores, Long typeId, Long specialtyId, Long clientId, Long statusId) {
		if (typeId != null && !projectTypeRepository.existsById(typeId)) {
			errores.put("typeId", "El tipo de proyecto indicado no existe.");
		}
		if (specialtyId != null && !projectSpecialtyRepository.existsById(specialtyId)) {
			errores.put("specialtyId", "La especialidad indicada no existe.");
		}
		if (clientId != null && !clientRepository.existsById(clientId)) {
			errores.put("clientId", "El mandante indicado no existe.");
		}
		if (statusId != null && !platformStatusRepository.existsById(statusId)) {
			errores.put("statusId", "El estado indicado no existe.");
		}
	}

	private void validarRangoFechas(Map<String, String> errores, LocalDate startDate, LocalDate endDate) {
		if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
			errores.put("endDate", "La fecha de termino no puede ser anterior a la fecha de inicio.");
		}
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
