package com.promaty.rrhh.services.termination.business.mapper;

import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.dto.termination.RelationSummaryDto;
import com.promaty.rrhh.dto.termination.StaffSummaryDto;
import com.promaty.rrhh.dto.termination.TerminationDetailDto;
import com.promaty.rrhh.dto.termination.TerminationListDto;
import com.promaty.rrhh.entity.NonRehireReason;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.entity.Termination;

public final class TerminationMapper {

	private TerminationMapper() {
	}

	public static TerminationListDto toListDto(Termination termination) {
		return new TerminationListDto(
			termination.getId(),
			toStaffSummary(termination.getStaff()),
			termination.getProject().getCostCenterCode(),
			termination.getLegalTerminationCause().getName(),
			termination.getTerminationDate(),
			termination.getDefinitiveTermination(),
			termination.getStatus().getCode(),
			termination.getStatus().getName(),
			termination.getCreatedAt(),
			termination.getUpdatedAt(),
			termination.getCreatedBy(),
			termination.getUpdatedBy(),
			null, // requestId se resuelve por fuera: Termination no tiene FK hacia Request, es al revés
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}

	public static TerminationDetailDto toDetailDto(Termination termination) {
		return new TerminationDetailDto(
			termination.getId(),
			toStaffSummary(termination.getStaff()),
			termination.getProject().getCostCenterCode(),
			termination.getContract().getJobTitle().getName(),
			termination.getContract().getStartDate(),
			termination.getContract().getEndDate(),
			termination.getTerminationDate(),
			new RelationSummaryDto(termination.getLegalTerminationCause().getId(), termination.getLegalTerminationCause().getName()),
			termination.getDefinitiveTermination(),
			toRelationSummary(termination.getNonRehireReason()),
			termination.getNotes(),
			new PlatformStatusOptionDto(
				termination.getStatus().getId(), termination.getStatus().getCode(), termination.getStatus().getName()
			),
			termination.getRexTerminationNumber(),
			termination.getCreatedAt(),
			termination.getUpdatedAt(),
			termination.getCreatedBy(),
			termination.getUpdatedBy(),
			null, // requestId se resuelve por fuera: Termination no tiene FK hacia Request, es al revés
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}

	private static StaffSummaryDto toStaffSummary(Staff staff) {
		return new StaffSummaryDto(staff.getId(), staff.getFullName(), staff.getIdentificationNumber());
	}

	private static RelationSummaryDto toRelationSummary(NonRehireReason nonRehireReason) {
		return nonRehireReason == null ? null : new RelationSummaryDto(nonRehireReason.getId(), nonRehireReason.getName());
	}
}
