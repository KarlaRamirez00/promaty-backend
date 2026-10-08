package com.promaty.rrhh.services.request.business.builder;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.services.shared.CurrentUserId;

@Component
public class CreateRequestBuilder {

	private final RequestRelationsResolver relationsResolver;
	private final ObjectMapper objectMapper;

	public CreateRequestBuilder(RequestRelationsResolver relationsResolver, ObjectMapper objectMapper) {
		this.relationsResolver = relationsResolver;
		this.objectMapper = objectMapper;
	}

	public Request build(CreateRequestDto dto) {
		Request request = new Request();
		request.setEntityType(dto.getEntityType());
		request.setAction(dto.getAction());
		request.setProject(relationsResolver.resolveProject(dto.getProjectId()));
		request.setRequesterUserId(CurrentUserId.get());
		request.setStatus(relationsResolver.resolveInitialStatus());
		request.setPendingData(serializar(dto));
		return request;
	}

	private String serializar(CreateRequestDto dto) {
		Object datos = switch (dto.getEntityType()) {
			case CONTRACT -> dto.getContractData();
		};
		try {
			return objectMapper.writeValueAsString(datos);
		} catch (JsonProcessingException e) {
			throw new BusinessValidationException(
				"No se pudo procesar los datos de la solicitud.",
				Map.of("contractData", "Formato de datos invalido.")
			);
		}
	}
}
