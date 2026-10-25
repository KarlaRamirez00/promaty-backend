package com.promaty.rrhh.repository.projection;

import java.time.LocalDate;

public interface StaffActiveContractDateRangeProjection {

	Long getStaffId();

	LocalDate getStartDate();

	LocalDate getEndDate();
}
