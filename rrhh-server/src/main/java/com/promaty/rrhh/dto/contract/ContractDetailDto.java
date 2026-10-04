package com.promaty.rrhh.dto.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.dto.shared.Action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractDetailDto {

	private Long id;
	private StaffSummaryDto staff;
	private RelationSummaryDto company;
	private RelationSummaryDto contractType;
	private RelationSummaryDto jobTitle;
	private RelationSummaryDto site;
	private String costCenterCode;
	private LocalDate startDate;
	private LocalDate endDate;
	private BigDecimal baseSalary;
	private BigDecimal agreedSalary;
	private Integer weeklyWorkHours;
	private Integer workDays;
	private String contractDetail;
	private RelationSummaryDto mealType;
	private RelationSummaryDto transportType;
	private PlatformStatusOptionDto status;
	private String name;
	private String contractNumber;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;
	private List<Action> actions;
}
