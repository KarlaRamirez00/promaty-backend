package com.promaty.rrhh.services.shared;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.RequestRepository;

@Component
public class RequestEditPolicy {

	private static final String CODE_PENDING_APPROVAL = "PENDING_APPROVAL";
	private static final Set<String> EDITABLE_CODES = Set.of(CODE_PENDING_APPROVAL, "PENDING_VALIDATION", "REJECTED");
	private static final String SUBMODULE_REQUEST = "request";

	private final RequestRepository requestRepository;
	private final PlatformStatusRepository platformStatusRepository;
	private final ObjectMapper objectMapper;

	public RequestEditPolicy(
		RequestRepository requestRepository,
		PlatformStatusRepository platformStatusRepository,
		ObjectMapper objectMapper
	) {
		this.requestRepository = requestRepository;
		this.platformStatusRepository = platformStatusRepository;
		this.objectMapper = objectMapper;
	}

	public boolean isEditable(Request request) {
		return request != null && EDITABLE_CODES.contains(request.getStatus().getCode());
	}

	public Request requireEditable(RequestEntityType entityType, Long entityId) {
		Request request = requestRepository.findByEntityTypeAndEntityId(entityType, entityId)
			.orElseThrow(() -> new ResourceNotFoundException("La solicitud de este registro no existe."));
		if (!isEditable(request)) {
			throw new BusinessValidationException(
				"Este registro no admite cambios.",
				Map.of("status", "Un registro aprobado o anulado no se puede editar.")
			);
		}
		return request;
	}

	public void replacePendingData(Request request, Object pendingData) {
		try {
			request.setPendingData(objectMapper.writeValueAsString(pendingData));
		} catch (JsonProcessingException e) {
			throw new BusinessValidationException(
				"No se pudo procesar los datos de la solicitud.", Map.of("pendingData", "Formato de datos invalido.")
			);
		}
		requestRepository.save(request);
	}

	public boolean returnToPendingApproval(Request request) {
		if (CODE_PENDING_APPROVAL.equals(request.getStatus().getCode())) {
			return false;
		}
		request.setStatus(pendingApprovalStatus(SUBMODULE_REQUEST));
		requestRepository.save(request);
		return true;
	}

	public PlatformStatus pendingApprovalStatus(String subModule) {
		return platformStatusRepository.findBySubModuleAndCode(subModule, CODE_PENDING_APPROVAL)
			.orElseThrow(() -> new ResourceNotFoundException("El estado PENDING_APPROVAL de " + subModule + " no está sembrado."));
	}
}
