package com.promaty.rrhh.services.contract.business.mapper;

import com.promaty.rrhh.dto.contract.ContractDetailDto;
import com.promaty.rrhh.dto.contract.ContractListDto;
import com.promaty.rrhh.dto.contract.RelationSummaryDto;
import com.promaty.rrhh.dto.contract.StaffSummaryDto;
import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.MealType;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.entity.TransportType;

public final class ContractMapper {

	private ContractMapper() {
	}

	public static ContractListDto toListDto(Contract contract) {
		return new ContractListDto(
			contract.getId(),
			toStaffSummary(contract.getStaff()),
			contract.getName(),
			contract.getContractNumber(),
			contract.getProject().getCostCenterCode(),
			contract.getContractType().getName(),
			contract.getStatus().getName(),
			contract.getStartDate(),
			contract.getEndDate(),
			contract.getCreatedAt(),
			contract.getUpdatedAt(),
			contract.getCreatedBy(),
			contract.getUpdatedBy(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}

	public static ContractDetailDto toDetailDto(Contract contract) {
		return new ContractDetailDto(
			contract.getId(),
			toStaffSummary(contract.getStaff()),
			new RelationSummaryDto(contract.getCompany().getId(), contract.getCompany().getName()),
			new RelationSummaryDto(contract.getContractType().getId(), contract.getContractType().getName()),
			new RelationSummaryDto(contract.getJobTitle().getId(), contract.getJobTitle().getName()),
			new RelationSummaryDto(contract.getSite().getId(), contract.getSite().getName()),
			contract.getProject().getCostCenterCode(),
			contract.getStartDate(),
			contract.getEndDate(),
			contract.getBaseSalary(),
			contract.getAgreedSalary(),
			contract.getWeeklyWorkHours(),
			contract.getWorkDays(),
			contract.getContractDetail(),
			toRelationSummary(contract.getMealType()),
			toRelationSummary(contract.getTransportType()),
			new PlatformStatusOptionDto(contract.getStatus().getId(), contract.getStatus().getCode(), contract.getStatus().getName()),
			contract.getName(),
			contract.getContractNumber(),
			contract.getCreatedAt(),
			contract.getUpdatedAt(),
			contract.getCreatedBy(),
			contract.getUpdatedBy(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}

	private static StaffSummaryDto toStaffSummary(Staff staff) {
		String fullName = staff.getFirstName() + " " + staff.getPaternalLastName() + " " + staff.getMaternalLastName();
		return new StaffSummaryDto(staff.getId(), fullName, staff.getIdentificationNumber());
	}

	private static RelationSummaryDto toRelationSummary(MealType mealType) {
		return mealType == null ? null : new RelationSummaryDto(mealType.getId(), mealType.getName());
	}

	private static RelationSummaryDto toRelationSummary(TransportType transportType) {
		return transportType == null ? null : new RelationSummaryDto(transportType.getId(), transportType.getName());
	}
}
