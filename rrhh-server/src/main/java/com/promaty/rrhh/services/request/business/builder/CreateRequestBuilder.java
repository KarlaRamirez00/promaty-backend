package com.promaty.rrhh.services.request.business.builder;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.services.shared.CurrentUserId;

@Component
public class CreateRequestBuilder {

	private static final String CODE_CONTRACT_ACTIVE = "ACTIVE";

	private final RequestRelationsResolver relationsResolver;
	private final ContractRepository contractRepository;
	private final ObjectMapper objectMapper;

	public CreateRequestBuilder(
		RequestRelationsResolver relationsResolver,
		ContractRepository contractRepository,
		ObjectMapper objectMapper
	) {
		this.relationsResolver = relationsResolver;
		this.contractRepository = contractRepository;
		this.objectMapper = objectMapper;
	}

	public Request build(CreateRequestDto dto) {
		Request request = new Request();
		request.setEntityType(dto.getEntityType());
		request.setAction(dto.getAction());
		request.setProject(resolveProject(dto));
		request.setRequesterUserId(CurrentUserId.get());
		request.setStatus(relationsResolver.resolveInitialStatus());
		request.setPendingData(serializar(dto));
		return request;
	}

	/**
	 * Para CONTRACT el centro de costo lo elige el solicitante (dto.projectId); para TERMINATION se
	 * deriva del contrato activo del colaborador — el cliente nunca lo envía (termination-
	 * architecture-guide.md §9: "The API must not accept... projectId... from the caller").
	 */
	private Project resolveProject(CreateRequestDto dto) {
		return switch (dto.getEntityType()) {
			case CONTRACT -> relationsResolver.resolveProject(dto.getProjectId());
			case TERMINATION -> contratoActivoDelStaff(dto.getTerminationData().getStaffId()).getProject();
		};
	}

	private Contract contratoActivoDelStaff(Long staffId) {
		return contractRepository.findByStaffIdAndStatus_Code(staffId, CODE_CONTRACT_ACTIVE)
			.orElseThrow(() -> new ResourceNotFoundException("El colaborador no tiene un contrato activo."));
	}

	private String serializar(CreateRequestDto dto) {
		Object datos = switch (dto.getEntityType()) {
			case CONTRACT -> dto.getContractData();
			case TERMINATION -> dto.getTerminationData();
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
