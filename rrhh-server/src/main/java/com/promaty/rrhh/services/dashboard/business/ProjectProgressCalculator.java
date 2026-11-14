package com.promaty.rrhh.services.dashboard.business;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.promaty.rrhh.dto.dashboard.ProjectProgressDto;
import com.promaty.rrhh.entity.Project;

public final class ProjectProgressCalculator {

	private static final int FULL_PERCENT = 100;

	private ProjectProgressCalculator() {
	}

	public static ProjectProgressDto calculate(Project project, LocalDate today) {
		LocalDate startDate = project.getStartDate();
		LocalDate endDate = project.getEndDate();
		if (endDate == null) {
			return toDto(project, null, null, null);
		}
		long daysRemaining = Math.max(ChronoUnit.DAYS.between(today, endDate), 0);
		long overdueDays = Math.max(ChronoUnit.DAYS.between(endDate, today), 0);
		return toDto(project, elapsedPercent(startDate, endDate, today), daysRemaining, overdueDays);
	}

	private static int elapsedPercent(LocalDate startDate, LocalDate endDate, LocalDate today) {
		long plannedDays = ChronoUnit.DAYS.between(startDate, endDate);
		long elapsedDays = ChronoUnit.DAYS.between(startDate, today);
		if (elapsedDays <= 0) {
			return 0;
		}
		if (plannedDays <= 0 || elapsedDays >= plannedDays) {
			return FULL_PERCENT;
		}
		return (int) (elapsedDays * FULL_PERCENT / plannedDays);
	}

	private static ProjectProgressDto toDto(Project project, Integer elapsedPercent, Long daysRemaining, Long overdueDays) {
		return new ProjectProgressDto(
			project.getId(),
			project.getName(),
			project.getCostCenterCode(),
			project.getStartDate(),
			project.getEndDate(),
			elapsedPercent,
			daysRemaining,
			overdueDays
		);
	}
}
