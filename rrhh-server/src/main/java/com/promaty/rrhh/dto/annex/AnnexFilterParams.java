package com.promaty.rrhh.dto.annex;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnexFilterParams {

	private String search;
	private Long projectId;
	private Long statusId;
	private Long annexTypeId;
	private LocalDate effectiveDateFrom;
	private LocalDate effectiveDateTo;
}
