package com.promaty.rrhh.dto.projecttype;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProjectTypeDto {

	@NotBlank(message = "El nombre es obligatorio.")
	private String name;
}
