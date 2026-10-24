package com.promaty.rrhh.dto.termination;

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
public class TerminationListDto {

	private Long id;
	private StaffSummaryDto staff;
	private String costCenterCode;
	private String legalTerminationCauseName;
	private LocalDate terminationDate;
	private Boolean definitiveTermination;
	private String statusCode;
	private String statusName;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;
	private Long requestId;
	private List<Action> actions;
}
