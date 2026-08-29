package com.promaty.rrhh.services.projectspecialty.business.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.projectspecialty.CreateProjectSpecialtyDto;
import com.promaty.rrhh.dto.projectspecialty.UpdateProjectSpecialtyDto;
import com.promaty.rrhh.entity.ProjectSpecialty;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.ProjectSpecialtyRepository;

@Component
public class ProjectSpecialtyValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";

	private final ProjectSpecialtyRepository projectSpecialtyRepository;

	public ProjectSpecialtyValidation(ProjectSpecialtyRepository projectSpecialtyRepository) {
		this.projectSpecialtyRepository = projectSpecialtyRepository;
	}

	public void validateCreate(CreateProjectSpecialtyDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		if (projectSpecialtyRepository.findByName(dto.getName()).isPresent()) {
			errores.put("name", "Ya existe una especialidad con este nombre.");
		}

		lanzarSiHayErrores(errores);
	}

	public void validateUpdate(Long id, UpdateProjectSpecialtyDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		Optional<ProjectSpecialty> existente = projectSpecialtyRepository.findByName(dto.getName());
		if (existente.isPresent() && !existente.get().getId().equals(id)) {
			errores.put("name", "Ya existe una especialidad con este nombre.");
		}

		lanzarSiHayErrores(errores);
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
