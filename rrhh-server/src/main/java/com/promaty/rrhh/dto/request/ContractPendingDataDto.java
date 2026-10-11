package com.promaty.rrhh.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractPendingDataDto {

	@NotNull(message = "El colaborador es obligatorio.")
	private Long staffId;

	@NotNull(message = "La empresa es obligatoria.")
	private Long companyId;

	@NotNull(message = "El tipo de contrato es obligatorio.")
	private Long contractTypeId;

	@NotNull(message = "El cargo es obligatorio.")
	private Long jobTitleId;

	@NotNull(message = "La sucursal es obligatoria.")
	private Long siteId;

	@NotNull(message = "La fecha de inicio es obligatoria.")
	private LocalDate startDate;

	private LocalDate endDate;

	@NotNull(message = "El sueldo base es obligatorio.")
	@DecimalMin(value = "0.0", inclusive = false, message = "El sueldo base debe ser mayor a 0.")
	private BigDecimal baseSalary;

	@DecimalMin(value = "0.0", inclusive = false, message = "El sueldo acordado debe ser mayor a 0.")
	private BigDecimal agreedSalary;

	@NotNull(message = "Las horas semanales son obligatorias.")
	@Min(value = 1, message = "Las horas semanales deben ser al menos 1.")
	@Max(value = 45, message = "Las horas semanales no pueden superar 45.")
	private Integer weeklyWorkHours;

	@NotNull(message = "Los días trabajados son obligatorios.")
	@Min(value = 5, message = "Los días trabajados deben ser 5 o 6.")
	@Max(value = 6, message = "Los días trabajados deben ser 5 o 6.")
	private Integer workDays;

	private String contractDetail;

	private Long mealTypeId;

	private Long transportTypeId;
}
