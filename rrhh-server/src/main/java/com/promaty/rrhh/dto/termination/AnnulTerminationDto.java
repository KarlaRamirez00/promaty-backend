package com.promaty.rrhh.dto.termination;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnulTerminationDto {

	@NotBlank(message = "El motivo de anulación es obligatorio.")
	@Size(max = 250, message = "El motivo de anulación no puede superar los 250 caracteres.")
	private String reason;
}
