package com.promaty.rrhh.services.annex.business.builder;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.annex.AnnexChangeFieldsDto;
import com.promaty.rrhh.dto.annex.UpdateAnnexDto;
import com.promaty.rrhh.entity.Annex;

@Component
public class UpdateAnnexBuilder {

	private final AnnexRelationsResolver relationsResolver;

	public UpdateAnnexBuilder(AnnexRelationsResolver relationsResolver) {
		this.relationsResolver = relationsResolver;
	}

	public void applyUpdate(Annex annex, UpdateAnnexDto dto) {
		if (dto.getAnnexTypeId() != null) {
			annex.setAnnexType(relationsResolver.resolveAnnexType(dto.getAnnexTypeId()));
		}
		apply(annex, dto);
	}

	public void apply(Annex annex, AnnexChangeFieldsDto dto) {
		annex.setEffectiveDate(dto.getEffectiveDate());
		annex.setDescription(dto.getDescription());
		annex.setNewEndDate(dto.getNewEndDate());
		annex.setNewBaseSalary(dto.getNewBaseSalary());
		annex.setNewJobTitle(relationsResolver.resolveJobTitle(dto.getNewJobTitleId()));
		annex.setNewWeeklyWorkHours(dto.getNewWeeklyWorkHours());
		annex.setNewWorkDays(dto.getNewWorkDays());
		annex.setNewSite(relationsResolver.resolveSite(dto.getNewSiteId()));
	}
}
