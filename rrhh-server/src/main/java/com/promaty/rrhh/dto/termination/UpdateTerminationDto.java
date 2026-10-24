package com.promaty.rrhh.dto.termination;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * staffId/contractId/projectId no están acá: son inmutables una vez creado el Finiquito
 * (termination-architecture-guide.md §5.2) — solo se edita el resto mientras siga pendiente.
 */
@Getter
@Setter
public class UpdateTerminationDto {

	@NotNull(message = "La fecha de término es obligatoria.")
	private LocalDate terminationDate;

	@NotNull(message = "La causal legal es obligatoria.")
	private Long legalTerminationCauseId;

	@NotNull(message = "Debe indicar si la desvinculación es definitiva.")
	private Boolean definitiveTermination;

	private Long nonRehireReasonId;

	@Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.")
	private String notes;
}
