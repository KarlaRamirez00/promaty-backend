package com.promaty.rrhh.services.staff.business.mapper;

import com.promaty.rrhh.dto.staff.RelationSummaryDto;
import com.promaty.rrhh.dto.staff.StaffDetailDto;
import com.promaty.rrhh.dto.staff.StaffListDto;
import com.promaty.rrhh.entity.Afp;
import com.promaty.rrhh.entity.Bank;
import com.promaty.rrhh.entity.Comuna;
import com.promaty.rrhh.entity.EducationLevel;
import com.promaty.rrhh.entity.HealthSystem;
import com.promaty.rrhh.entity.MaritalStatus;
import com.promaty.rrhh.entity.Nationality;
import com.promaty.rrhh.entity.Provincia;
import com.promaty.rrhh.entity.Region;
import com.promaty.rrhh.entity.RegisteredSex;
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
			toRelationSummary(staff.getRegisteredSex()),
			toRelationSummary(staff.getMaritalStatus()),
			toRelationSummary(staff.getNationality()),
			staff.getPhone1(),
			staff.getEmergencyPhone(),
			staff.getEmergencyContactName(),
			staff.getAddress(),
			toRelationSummary(staff.getComuna().getProvincia().getRegion()),
			toRelationSummary(staff.getComuna().getProvincia()),
			toRelationSummary(staff.getComuna()),
			staff.getHasChildren(),
			staff.getChildrenCount(),
			staff.getPersonalEmail(),
			staff.getShoeSize(),
			staff.getClothingSize(),
			toRelationSummary(staff.getEducationLevel()),
			toRelationSummary(staff.getAfp()),
			toRelationSummary(staff.getHealthSystem()),
			toRelationSummary(staff.getBank()),
			staff.getAccountType(),
			staff.getAccountNumber(),
			staff.getCreatedAt(),
			staff.getUpdatedAt(),
			staff.getCreatedBy(),
			staff.getUpdatedBy(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}

	private static RelationSummaryDto toRelationSummary(RegisteredSex registeredSex) {
		return new RelationSummaryDto(registeredSex.getId(), registeredSex.getName(), registeredSex.getCode());
	}

	private static RelationSummaryDto toRelationSummary(MaritalStatus maritalStatus) {
		return new RelationSummaryDto(maritalStatus.getId(), maritalStatus.getName(), maritalStatus.getCode());
	}

	private static RelationSummaryDto toRelationSummary(Nationality nationality) {
		return new RelationSummaryDto(nationality.getId(), nationality.getName(), nationality.getCode());
	}

	private static RelationSummaryDto toRelationSummary(EducationLevel educationLevel) {
		return new RelationSummaryDto(educationLevel.getId(), educationLevel.getName(), educationLevel.getCode());
	}

	private static RelationSummaryDto toRelationSummary(Afp afp) {
		return new RelationSummaryDto(afp.getId(), afp.getName(), afp.getCode());
	}

	private static RelationSummaryDto toRelationSummary(HealthSystem healthSystem) {
		return new RelationSummaryDto(healthSystem.getId(), healthSystem.getName(), healthSystem.getCode());
	}

	private static RelationSummaryDto toRelationSummary(Bank bank) {
		return new RelationSummaryDto(bank.getId(), bank.getName(), bank.getCode());
	}

	private static RelationSummaryDto toRelationSummary(Region region) {
		return new RelationSummaryDto(region.getId(), region.getName(), region.getCode());
	}

	private static RelationSummaryDto toRelationSummary(Provincia provincia) {
		return new RelationSummaryDto(provincia.getId(), provincia.getName(), provincia.getCode());
	}

	private static RelationSummaryDto toRelationSummary(Comuna comuna) {
		return new RelationSummaryDto(comuna.getId(), comuna.getName(), comuna.getCode());
	}
}
