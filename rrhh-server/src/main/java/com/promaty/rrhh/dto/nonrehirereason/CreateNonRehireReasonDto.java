package com.promaty.rrhh.dto.nonrehirereason;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNonRehireReasonDto {

	@NotBlank(message = "El nombre es obligatorio.")
	@Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
	private String name;

	@NotBlank(message = "El código es obligatorio.")
	@Size(max = 40, message = "El código no puede superar los 40 caracteres.")
	private String code;

	@Size(max = 250, message = "La descripción no puede superar los 250 caracteres.")
	private String description;
}
