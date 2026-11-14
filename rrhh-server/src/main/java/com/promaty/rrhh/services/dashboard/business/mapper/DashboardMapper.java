package com.promaty.rrhh.services.dashboard.business.mapper;

import java.time.LocalDate;

import com.promaty.rrhh.dto.dashboard.BirthdayDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Staff;

public final class DashboardMapper {

	private DashboardMapper() {
	}

	public static BirthdayDto toBirthdayDto(Contract activeContract) {
		Staff staff = activeContract.getStaff();
		LocalDate birthDate = staff.getBirthDate();
		return new BirthdayDto(
			staff.getId(),
			staff.getFullName(),
			birthDate.getDayOfMonth(),
			birthDate.getMonthValue(),
			activeContract.getProject().getCostCenterCode()
		);
	}
}
