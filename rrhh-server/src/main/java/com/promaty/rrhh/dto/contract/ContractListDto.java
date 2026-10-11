package com.promaty.rrhh.dto.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.promaty.rrhh.dto.shared.Action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractListDto {

	private Long id;
	private StaffSummaryDto staff;
	private String name;
	private String contractNumber;
	private String costCenterCode;
	private String contractTypeName;
	private String jobTitleName;
	private String statusCode;
	private String statusName;
	private LocalDate startDate;
	private LocalDate endDate;
	private BigDecimal agreedSalary;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;
	private List<Action> actions;
}
