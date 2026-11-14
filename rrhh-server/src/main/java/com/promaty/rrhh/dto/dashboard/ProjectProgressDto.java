package com.promaty.rrhh.dto.dashboard;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectProgressDto {

	private Long id;
	private String name;
	private String costCenterCode;
	private LocalDate startDate;
	private LocalDate endDate;
	private Integer elapsedPercent;
	private Long daysRemaining;
	private Long overdueDays;
}
