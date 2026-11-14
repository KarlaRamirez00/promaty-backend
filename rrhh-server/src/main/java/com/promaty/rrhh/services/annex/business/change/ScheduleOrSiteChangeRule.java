package com.promaty.rrhh.services.annex.business.change;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexChangeValues;
import com.promaty.rrhh.entity.AnnexTypeCode;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Site;
import com.promaty.rrhh.repository.SiteRepository;

@Component
public class ScheduleOrSiteChangeRule implements AnnexChangeRule {

	private static final String FIELD_NEW_WEEKLY_WORK_HOURS = "newWeeklyWorkHours";
	private static final String FIELD_NEW_WORK_DAYS = "newWorkDays";
	private static final String FIELD_NEW_SITE = "newSiteId";
	private static final String SAME_AS_CURRENT = "Es igual al valor actual del contrato.";

	private final SiteRepository siteRepository;

	public ScheduleOrSiteChangeRule(SiteRepository siteRepository) {
		this.siteRepository = siteRepository;
	}

	@Override
	public AnnexTypeCode supports() {
		return AnnexTypeCode.SCHEDULE_OR_SITE_CHANGE;
	}

	@Override
	public Set<AnnexChangeField> fields() {
		return Set.of(AnnexChangeField.NEW_WEEKLY_WORK_HOURS, AnnexChangeField.NEW_WORK_DAYS, AnnexChangeField.NEW_SITE);
	}

	@Override
	public void validate(AnnexChangeValues values, Contract contract, Map<String, String> errors) {
		Integer newWeeklyWorkHours = values.getNewWeeklyWorkHours();
		Integer newWorkDays = values.getNewWorkDays();
		Long newSiteId = values.getNewSiteId();
		if (newWeeklyWorkHours == null && newWorkDays == null && newSiteId == null) {
			errors.put(FIELD_NEW_WEEKLY_WORK_HOURS, "Indica al menos un cambio: horas semanales, días trabajados o sucursal.");
			return;
		}
		rejectIfUnchanged(newWeeklyWorkHours, contract.getWeeklyWorkHours(), FIELD_NEW_WEEKLY_WORK_HOURS, errors);
		rejectIfUnchanged(newWorkDays, contract.getWorkDays(), FIELD_NEW_WORK_DAYS, errors);
		if (newSiteId != null) {
			validateSite(newSiteId, contract, errors);
		}
	}

	@Override
	public void apply(Annex annex, Contract contract) {
		if (annex.getNewWeeklyWorkHours() != null) {
			annex.setPreviousWeeklyWorkHours(contract.getWeeklyWorkHours());
			contract.setWeeklyWorkHours(annex.getNewWeeklyWorkHours());
		}
		if (annex.getNewWorkDays() != null) {
			annex.setPreviousWorkDays(contract.getWorkDays());
			contract.setWorkDays(annex.getNewWorkDays());
		}
		if (annex.getNewSite() != null) {
			annex.setPreviousSite(contract.getSite());
			contract.setSite(annex.getNewSite());
		}
	}

	private void validateSite(Long newSiteId, Contract contract, Map<String, String> errors) {
		Optional<Site> site = siteRepository.findById(newSiteId);
		if (site.isEmpty()) {
			errors.put(FIELD_NEW_SITE, "La sucursal indicada no existe.");
		} else if (!Boolean.TRUE.equals(site.get().getActive())) {
			errors.put(FIELD_NEW_SITE, "La sucursal indicada no está activa.");
		} else {
			rejectIfUnchanged(newSiteId, contract.getSite().getId(), FIELD_NEW_SITE, errors);
		}
	}

	private void rejectIfUnchanged(Object newValue, Object currentValue, String field, Map<String, String> errors) {
		if (newValue != null && Objects.equals(newValue, currentValue)) {
			errors.put(field, SAME_AS_CURRENT);
		}
	}
}
