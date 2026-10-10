package com.promaty.rrhh.dto.requestrejectionreason;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRequestRejectionReasonDto {

	@NotBlank(message = "El nombre es obligatorio.")
	@Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
	private String name;

	@Size(max = 250, message = "La descripcion no puede superar los 250 caracteres.")
	private String description;

	@NotBlank(message = "El submodulo es obligatorio.")
	private String subModule;
}
