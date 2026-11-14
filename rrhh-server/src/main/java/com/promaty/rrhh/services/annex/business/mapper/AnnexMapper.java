package com.promaty.rrhh.services.annex.business.mapper;

import com.promaty.rrhh.dto.annex.AnnexDetailDto;
import com.promaty.rrhh.dto.annex.AnnexListDto;
import com.promaty.rrhh.dto.contract.RelationSummaryDto;
import com.promaty.rrhh.dto.contract.StaffSummaryDto;
import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexType;
import com.promaty.rrhh.entity.ContractType;
import com.promaty.rrhh.entity.JobTitle;
import com.promaty.rrhh.entity.Site;
import com.promaty.rrhh.entity.Staff;

public final class AnnexMapper {

	private AnnexMapper() {
	}

	public static CatalogOptionDto toTypeOptionDto(AnnexType annexType) {
		return new CatalogOptionDto(annexType.getId(), annexType.getName(), annexType.getCode().name());
	}

	public static AnnexListDto toListDto(Annex annex) {
		return new AnnexListDto(
			annex.getId(),
			toStaffSummary(annex.getStaff()),
			annex.getProject().getCostCenterCode(),
			toTypeOptionDto(annex.getAnnexType()),
			annex.getNewJobTitle() == null ? null : annex.getNewJobTitle().getName(),
			annex.getNewBaseSalary(),
			annex.getNewEndDate(),
			annex.getEffectiveDate(),
			annex.getStatus().getCode(),
			annex.getStatus().getName(),
			annex.getCreatedAt(),
			annex.getUpdatedAt(),
			annex.getCreatedBy(),
			annex.getUpdatedBy(),
			null, // requestId se resuelve por fuera: Annex no tiene FK hacia Request, es al revés
			null  // actions depende de los permisos del usuario que pide: lo completa el service
		);
	}

	public static AnnexDetailDto toDetailDto(Annex annex) {
		return new AnnexDetailDto(
			annex.getId(),
			toStaffSummary(annex.getStaff()),
			annex.getContract().getId(),
			annex.getProject().getCostCenterCode(),
			toTypeOptionDto(annex.getAnnexType()),
			annex.getEffectiveDate(),
			annex.getDescription(),
			annex.getNewEndDate(),
			annex.getNewBaseSalary(),
			toRelationSummary(annex.getNewJobTitle()),
			annex.getNewWeeklyWorkHours(),
			annex.getNewWorkDays(),
			toRelationSummary(annex.getNewSite()),
			toRelationSummary(annex.getPreviousContractType()),
			annex.getPreviousEndDate(),
			annex.getPreviousBaseSalary(),
			toRelationSummary(annex.getPreviousJobTitle()),
			annex.getPreviousWeeklyWorkHours(),
			annex.getPreviousWorkDays(),
			toRelationSummary(annex.getPreviousSite()),
			new PlatformStatusOptionDto(annex.getStatus().getId(), annex.getStatus().getCode(), annex.getStatus().getName()),
			annex.getRexAnnexNumber(),
			annex.getCreatedAt(),
			annex.getUpdatedAt(),
			annex.getCreatedBy(),
			annex.getUpdatedBy(),
			null, // requestId se resuelve por fuera: Annex no tiene FK hacia Request, es al revés
			null, // actions depende de los permisos del usuario que pide: lo completa el service
			null  // attachments se carga por separado en el service
		);
	}

	private static StaffSummaryDto toStaffSummary(Staff staff) {
		return new StaffSummaryDto(staff.getId(), staff.getFullName(), staff.getIdentificationNumber());
	}

	private static RelationSummaryDto toRelationSummary(JobTitle jobTitle) {
		return jobTitle == null ? null : new RelationSummaryDto(jobTitle.getId(), jobTitle.getName());
	}

	private static RelationSummaryDto toRelationSummary(Site site) {
		return site == null ? null : new RelationSummaryDto(site.getId(), site.getName());
	}

	private static RelationSummaryDto toRelationSummary(ContractType contractType) {
		return contractType == null ? null : new RelationSummaryDto(contractType.getId(), contractType.getName());
	}
}
