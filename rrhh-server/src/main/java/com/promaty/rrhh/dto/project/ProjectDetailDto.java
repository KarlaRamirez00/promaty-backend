package com.promaty.rrhh.dto.project;

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
public class ProjectDetailDto {

	private Long id;
	private String name;
	private String costCenterCode;
	private RelationSummaryDto type;
	private RelationSummaryDto specialty;
	private RelationSummaryDto client;
	private PlatformStatusOptionDto status;
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<Action> actions;
}
