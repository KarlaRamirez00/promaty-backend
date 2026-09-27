package com.promaty.rrhh.services.staff.business.mapper;

import com.promaty.rrhh.dto.staff.StaffDetailDto;
import com.promaty.rrhh.dto.staff.StaffListDto;
import com.promaty.rrhh.entity.Staff;

public final class StaffMapper {

	private StaffMapper() {
	}

	public static StaffListDto toListDto(Staff staff) {
		return new StaffListDto(
			staff.getId(),
			staff.getIdentificationType(),
			staff.getIdentificationNumber(),
			staff.getFirstName(),
			staff.getPaternalLastName(),
			staff.getMaternalLastName(),
			staff.getCreatedAt(),
			staff.getUpdatedAt(),
			staff.getCreatedBy(),
			staff.getUpdatedBy(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}

	public static StaffDetailDto toDetailDto(Staff staff) {
		return new StaffDetailDto(
			staff.getId(),
			staff.getIdentificationType(),
			staff.getIdentificationNumber(),
			staff.getFirstName(),
			staff.getPaternalLastName(),
			staff.getMaternalLastName(),
			staff.getBirthDate(),
			staff.getPersonalEmail(),
			staff.getPhone1(),
			staff.getCreatedAt(),
			staff.getUpdatedAt(),
			staff.getCreatedBy(),
			staff.getUpdatedBy(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}
}
