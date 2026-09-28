package com.promaty.rrhh.services.staff.business.builder;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.staff.CreateStaffDto;
import com.promaty.rrhh.entity.Staff;

@Component
public class CreateStaffBuilder {

	private final StaffRelationsResolver relationsResolver;

	public CreateStaffBuilder(StaffRelationsResolver relationsResolver) {
		this.relationsResolver = relationsResolver;
	}

	public Staff build(CreateStaffDto dto) {
		Staff staff = new Staff();
		staff.setIdentificationType(dto.getIdentificationType());
		staff.setIdentificationNumber(dto.getIdentificationNumber());
		staff.setFirstName(dto.getFirstName());
		staff.setPaternalLastName(dto.getPaternalLastName());
		staff.setMaternalLastName(dto.getMaternalLastName());
		staff.setBirthDate(dto.getBirthDate());
		staff.setRegisteredSex(relationsResolver.resolveRegisteredSex(dto.getRegisteredSexId()));
		staff.setMaritalStatus(relationsResolver.resolveMaritalStatus(dto.getMaritalStatusId()));
		staff.setNationality(relationsResolver.resolveNationality(dto.getNationalityId()));
		staff.setPhone1(dto.getPhone1());
		staff.setEmergencyPhone(dto.getEmergencyPhone());
		staff.setEmergencyContactName(dto.getEmergencyContactName());
		staff.setAddress(dto.getAddress());
		staff.setCity(dto.getCity());
		staff.setHasChildren(dto.getHasChildren());
		staff.setChildrenCount(dto.getChildrenCount());
		staff.setPersonalEmail(dto.getPersonalEmail());
		staff.setShoeSize(dto.getShoeSize());
		staff.setClothingSize(dto.getClothingSize());
		staff.setEducationLevel(relationsResolver.resolveEducationLevel(dto.getEducationLevelId()));
		staff.setAfp(relationsResolver.resolveAfp(dto.getAfpId()));
		staff.setHealthSystem(relationsResolver.resolveHealthSystem(dto.getHealthSystemId()));
		staff.setBank(relationsResolver.resolveBank(dto.getBankId()));
		staff.setAccountType(dto.getAccountType());
		staff.setAccountNumber(dto.getAccountNumber());
		return staff;
	}
}
