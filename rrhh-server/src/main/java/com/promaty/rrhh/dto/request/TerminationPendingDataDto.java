package com.promaty.rrhh.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TerminationPendingDataDto {

	@NotNull(message = "El colaborador es obligatorio.")
	private Long staffId;

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
