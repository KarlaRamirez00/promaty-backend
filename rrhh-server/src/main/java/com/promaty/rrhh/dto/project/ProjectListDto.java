package com.promaty.rrhh.dto.project;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectListDto {

	private Long id;
	private String name;
	private String costCenterCode;
	private String typeName;
	private String specialtyName;
	private String clientName;
	private String statusName;
	private LocalDate startDate;
	private LocalDate endDate;
}
