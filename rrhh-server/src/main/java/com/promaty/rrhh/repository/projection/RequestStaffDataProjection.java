package com.promaty.rrhh.repository.projection;

import com.promaty.rrhh.entity.IdentificationType;

public interface RequestStaffDataProjection {
	Long getContractId();
	IdentificationType getStaffIdentificationType();
	String getStaffIdentificationNumber();
	String getStaffFirstName();
	String getStaffPaternalLastName();
	String getStaffMaternalLastName();
	String getJobTitleName();
}
