package com.promaty.rrhh.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

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
	private BigDecimal baseSalary;

	private BigDecimal agreedSalary;

	@NotNull(message = "Las horas semanales son obligatorias.")
	private Integer weeklyWorkHours;

	@NotNull(message = "Los días trabajados son obligatorios.")
	private Integer workDays;

	private String contractDetail;

	private Long mealTypeId;

	private Long transportTypeId;
}
