package com.promaty.rrhh.dto.projectspecialty;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProjectSpecialtyDto {

	@NotBlank(message = "El nombre es obligatorio.")
	private String name;
}
