package com.promaty.rrhh.dto.annex;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.promaty.rrhh.entity.AnnexChangeValues;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AnnexChangeFieldsDto implements AnnexChangeValues {

	@NotNull(message = "La fecha de vigencia es obligatoria.")
	private LocalDate effectiveDate;

	@NotBlank(message = "La descripción es obligatoria.")
	@Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
	private String description;

	private LocalDate newEndDate;

	@DecimalMin(value = "0.0", inclusive = false, message = "El nuevo sueldo base debe ser mayor a 0.")
	@Digits(integer = 15, fraction = 0, message = "El nuevo sueldo base admite hasta 15 dígitos, sin decimales.")
	private BigDecimal newBaseSalary;

	private Long newJobTitleId;

	@Min(value = 1, message = "Las horas semanales deben ser al menos 1.")
	@Max(value = 45, message = "Las horas semanales no pueden superar 45.")
	private Integer newWeeklyWorkHours;

	@Min(value = 5, message = "Los días trabajados deben ser 5 o 6.")
	@Max(value = 6, message = "Los días trabajados deben ser 5 o 6.")
	private Integer newWorkDays;

	private Long newSiteId;
}
