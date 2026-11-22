package com.promaty.rrhh.dto.annex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.promaty.rrhh.dto.contract.ContractSummaryDto;
import com.promaty.rrhh.dto.contract.RelationSummaryDto;
import com.promaty.rrhh.dto.contract.StaffSummaryDto;
import com.promaty.rrhh.dto.file.AttachmentRequirementDto;
import com.promaty.rrhh.dto.file.FileAttachmentDto;
import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
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
public class AnnexDetailDto {

	private Long id;
	private StaffSummaryDto staff;
	private Long contractId;
	private String costCenterCode;
	private RelationSummaryDto project;
	private ContractSummaryDto currentContract;
	private CatalogOptionDto annexType;
	private LocalDate effectiveDate;
	private String description;
	private LocalDate newEndDate;
	private BigDecimal newBaseSalary;
	private RelationSummaryDto newJobTitle;
	private Integer newWeeklyWorkHours;
	private Integer newWorkDays;
	private RelationSummaryDto newSite;
	private RelationSummaryDto previousContractType;
	private LocalDate previousEndDate;
	private BigDecimal previousBaseSalary;
	private RelationSummaryDto previousJobTitle;
	private Integer previousWeeklyWorkHours;
	private Integer previousWorkDays;
	private RelationSummaryDto previousSite;
	private PlatformStatusOptionDto status;
	private String rexAnnexNumber;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;
	private Long requestId;
	private List<Action> actions;
	private List<FileAttachmentDto> attachments;
	private List<AttachmentRequirementDto> attachmentRequirements;
	private String syncErrorMessage;
}
