package com.promaty.rrhh.dto.termination;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TerminationFilterParams {

	private String search;
	private Long projectId;
	private Long statusId;
	private LocalDate terminationDateFrom;
	private LocalDate terminationDateTo;
}
