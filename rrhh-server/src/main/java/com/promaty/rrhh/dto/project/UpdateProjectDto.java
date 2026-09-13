package com.promaty.rrhh.dto.project;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProjectDto {

	@NotBlank(message = "El nombre es obligatorio.")
	private String name;

	@NotBlank(message = "El centro de costo es obligatorio.")
	private String costCenterCode;

	@NotNull(message = "El tipo de proyecto es obligatorio.")
	private Long typeId;

	@NotNull(message = "La especialidad es obligatoria.")
	private Long specialtyId;

	@NotNull(message = "El mandante es obligatorio.")
	private Long clientId;

	@NotNull(message = "La fecha de inicio es obligatoria.")
	private LocalDate startDate;

	private LocalDate endDate;
}
