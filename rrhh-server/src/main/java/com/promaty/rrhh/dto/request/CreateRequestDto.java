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

	/**
	 * Obligatorio solo para entityType=CONTRACT (validado en RequestValidation, no acá): para
	 * TERMINATION el centro de costo se deriva en el servidor desde el contrato activo del
	 * colaborador, nunca se acepta del cliente (ver termination-architecture-guide.md §9).
	 */
	private Long projectId;

	@Valid
	private ContractPendingDataDto contractData;

	@Valid
	private TerminationPendingDataDto terminationData;
}
