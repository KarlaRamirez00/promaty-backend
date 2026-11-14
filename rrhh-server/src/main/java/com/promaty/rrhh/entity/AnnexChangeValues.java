package com.promaty.rrhh.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface AnnexChangeValues {

	LocalDate getNewEndDate();

	BigDecimal getNewBaseSalary();

	Long getNewJobTitleId();

	Integer getNewWeeklyWorkHours();

	Integer getNewWorkDays();

	Long getNewSiteId();
}
