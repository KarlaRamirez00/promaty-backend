package com.promaty.rrhh.dto.request;

import java.time.LocalDate;

import com.promaty.rrhh.dto.contract.RelationSummaryDto;
import com.promaty.rrhh.dto.contract.StaffSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TerminationPendingDataResolvedDto {

	private StaffSummaryDto staff;
	private LocalDate terminationDate;
	private RelationSummaryDto legalTerminationCause;
	private Boolean definitiveTermination;
	private RelationSummaryDto nonRehireReason;
	private String notes;
}
