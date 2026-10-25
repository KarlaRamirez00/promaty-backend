package com.promaty.rrhh.dto.termination;

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
public class TerminationDetailDto {

	private Long id;
	private StaffSummaryDto staff;
	private String costCenterCode;
	private String jobTitleName;
	private LocalDate contractStartDate;
	private LocalDate contractEndDate;
	private LocalDate terminationDate;
	private RelationSummaryDto legalTerminationCause;
	private Boolean definitiveTermination;
	private RelationSummaryDto nonRehireReason;
	private String notes;
	private PlatformStatusOptionDto status;
	private String rexTerminationNumber;
	private String annulmentReason;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;
	private Long requestId;
	private List<Action> actions;
}
