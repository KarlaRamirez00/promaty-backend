package com.promaty.rrhh.services.annex.business.builder;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.request.AnnexPendingDataDto;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.Project;

@Component
public class CreateAnnexBuilder {

	private static final String CODE_PENDING_APPROVAL = "PENDING_APPROVAL";

	private final AnnexRelationsResolver relationsResolver;
	private final UpdateAnnexBuilder updateAnnexBuilder;

	public CreateAnnexBuilder(AnnexRelationsResolver relationsResolver, UpdateAnnexBuilder updateAnnexBuilder) {
		this.relationsResolver = relationsResolver;
		this.updateAnnexBuilder = updateAnnexBuilder;
	}

	public Annex build(AnnexPendingDataDto dto, Project project) {
		Annex annex = new Annex();
		annex.setStaff(relationsResolver.resolveStaff(dto.getStaffId()));
		annex.setContract(relationsResolver.resolveActiveContract(dto.getStaffId()));
		annex.setProject(project);
		annex.setAnnexType(relationsResolver.resolveAnnexType(dto.getAnnexTypeId()));
		annex.setStatus(relationsResolver.resolveStatus(CODE_PENDING_APPROVAL));
		updateAnnexBuilder.apply(annex, dto);
		return annex;
	}
}
