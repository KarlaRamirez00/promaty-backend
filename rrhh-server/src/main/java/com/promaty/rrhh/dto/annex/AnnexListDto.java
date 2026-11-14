package com.promaty.rrhh.dto.annex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.promaty.rrhh.dto.contract.StaffSummaryDto;
import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnnexListDto {

	private Long id;
	private StaffSummaryDto staff;
	private String costCenterCode;
	private CatalogOptionDto annexType;
	private String newJobTitleName;
	private BigDecimal newBaseSalary;
	private LocalDate newEndDate;
	private LocalDate effectiveDate;
	private String statusCode;
	private String statusName;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;
	private Long requestId;
	private List<Action> actions;
}
