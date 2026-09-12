package com.promaty.rrhh.dto.projecttype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProjectTypeDto {

	@NotBlank(message = "El nombre es obligatorio.")
	@Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
	private String name;
}
