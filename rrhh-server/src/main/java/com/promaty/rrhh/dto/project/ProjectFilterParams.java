package com.promaty.rrhh.dto.project;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectFilterParams {

	private String search;
	private Long typeId;
	private Long specialtyId;
	private Long clientId;
	private Long statusId;
	private LocalDate startDateFrom;
	private LocalDate startDateTo;
}
