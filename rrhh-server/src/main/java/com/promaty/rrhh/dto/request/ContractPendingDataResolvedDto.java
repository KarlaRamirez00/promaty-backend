package com.promaty.rrhh.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.promaty.rrhh.dto.contract.RelationSummaryDto;
import com.promaty.rrhh.dto.contract.StaffSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContractPendingDataResolvedDto {

	private StaffSummaryDto staff;
	private RelationSummaryDto company;
	private RelationSummaryDto contractType;
	private RelationSummaryDto jobTitle;
	private RelationSummaryDto site;
	private LocalDate startDate;
	private LocalDate endDate;
	private BigDecimal baseSalary;
	private BigDecimal agreedSalary;
	private Integer weeklyWorkHours;
	private Integer workDays;
	private String contractDetail;
	private RelationSummaryDto mealType;
	private RelationSummaryDto transportType;
}
