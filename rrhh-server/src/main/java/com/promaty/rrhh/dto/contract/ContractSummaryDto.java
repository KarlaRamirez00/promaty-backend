package com.promaty.rrhh.dto.contract;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContractSummaryDto {

	private Long contractId;
	private CatalogOptionDto contractType;
	private LocalDate startDate;
	private LocalDate endDate;
	private BigDecimal baseSalary;
	private RelationSummaryDto jobTitle;
	private Integer weeklyWorkHours;
	private Integer workDays;
	private RelationSummaryDto site;
	private RelationSummaryDto project;
}
