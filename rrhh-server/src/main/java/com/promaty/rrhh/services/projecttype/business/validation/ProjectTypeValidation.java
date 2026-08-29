package com.promaty.rrhh.services.projecttype.business.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.projecttype.CreateProjectTypeDto;
import com.promaty.rrhh.dto.projecttype.UpdateProjectTypeDto;
import com.promaty.rrhh.entity.ProjectType;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.ProjectTypeRepository;

@Component
public class ProjectTypeValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";

	private final ProjectTypeRepository projectTypeRepository;

	public ProjectTypeValidation(ProjectTypeRepository projectTypeRepository) {
		this.projectTypeRepository = projectTypeRepository;
	}

	public void validateCreate(CreateProjectTypeDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		if (projectTypeRepository.findByName(dto.getName()).isPresent()) {
			errores.put("name", "Ya existe un tipo de proyecto con este nombre.");
		}

		lanzarSiHayErrores(errores);
	}

	public void validateUpdate(Long id, UpdateProjectTypeDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		Optional<ProjectType> existente = projectTypeRepository.findByName(dto.getName());
		if (existente.isPresent() && !existente.get().getId().equals(id)) {
			errores.put("name", "Ya existe un tipo de proyecto con este nombre.");
		}

		lanzarSiHayErrores(errores);
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
