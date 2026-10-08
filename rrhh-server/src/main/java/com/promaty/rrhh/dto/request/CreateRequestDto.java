package com.promaty.rrhh.dto.request;

import com.promaty.rrhh.entity.RequestAction;
import com.promaty.rrhh.entity.RequestEntityType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRequestDto {

	@NotNull(message = "El tipo de entidad es obligatorio.")
	private RequestEntityType entityType;

	@NotNull(message = "La acción es obligatoria.")
	private RequestAction action;

	@NotNull(message = "El centro de costo es obligatorio.")
	private Long projectId;

	@Valid
	private ContractPendingDataDto contractData;
}
