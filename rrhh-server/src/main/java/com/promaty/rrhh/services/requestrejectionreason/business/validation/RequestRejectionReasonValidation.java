package com.promaty.rrhh.services.requestrejectionreason.business.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.requestrejectionreason.CreateRequestRejectionReasonDto;
import com.promaty.rrhh.dto.requestrejectionreason.UpdateRequestRejectionReasonDto;
import com.promaty.rrhh.entity.RequestRejectionReason;
import com.promaty.rrhh.entity.RequestRejectionReasonSubModule;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.RequestRejectionReasonRepository;

@Component
public class RequestRejectionReasonValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";

	private final RequestRejectionReasonRepository requestRejectionReasonRepository;

	public RequestRejectionReasonValidation(RequestRejectionReasonRepository requestRejectionReasonRepository) {
		this.requestRejectionReasonRepository = requestRejectionReasonRepository;
	}

	public void validateCreate(CreateRequestRejectionReasonDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		if (requestRejectionReasonRepository.findByName(dto.getName()).isPresent()) {
			errores.put("name", "Ya existe un motivo de rechazo con este nombre.");
		}
		validarSubModulo(dto.getSubModule(), errores);

		lanzarSiHayErrores(errores);
	}

	public void validateUpdate(Long id, UpdateRequestRejectionReasonDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		Optional<RequestRejectionReason> existente = requestRejectionReasonRepository.findByName(dto.getName());
		if (existente.isPresent() && !existente.get().getId().equals(id)) {
			errores.put("name", "Ya existe un motivo de rechazo con este nombre.");
		}
		validarSubModulo(dto.getSubModule(), errores);

		lanzarSiHayErrores(errores);
	}

	private void validarSubModulo(String subModule, Map<String, String> errores) {
		if (RequestRejectionReasonSubModule.fromCode(subModule).isEmpty()) {
			errores.put("subModule", "El submodulo indicado no existe.");
		}
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
