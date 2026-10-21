package com.promaty.rrhh.repository.projection;

import java.time.LocalDate;

public interface StaffContractStartDateProjection {

	Long getStaffId();

	LocalDate getStartDate();
}
