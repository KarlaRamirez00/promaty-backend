package com.promaty.rrhh.dto.project;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProjectStatusDto {

	@NotNull(message = "El estado es obligatorio.")
	private Long statusId;
}
