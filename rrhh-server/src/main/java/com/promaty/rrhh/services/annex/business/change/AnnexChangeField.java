package com.promaty.rrhh.services.annex.business.change;

import java.util.function.Function;

import com.promaty.rrhh.entity.AnnexChangeValues;

public enum AnnexChangeField {

	NEW_END_DATE("newEndDate", AnnexChangeValues::getNewEndDate),
	NEW_BASE_SALARY("newBaseSalary", AnnexChangeValues::getNewBaseSalary),
	NEW_JOB_TITLE("newJobTitleId", AnnexChangeValues::getNewJobTitleId),
	NEW_WEEKLY_WORK_HOURS("newWeeklyWorkHours", AnnexChangeValues::getNewWeeklyWorkHours),
	NEW_WORK_DAYS("newWorkDays", AnnexChangeValues::getNewWorkDays),
	NEW_SITE("newSiteId", AnnexChangeValues::getNewSiteId);

	private final String fieldName;
	private final Function<AnnexChangeValues, Object> reader;

	AnnexChangeField(String fieldName, Function<AnnexChangeValues, Object> reader) {
		this.fieldName = fieldName;
		this.reader = reader;
	}

	public String getFieldName() {
		return fieldName;
	}

	public boolean isPresentIn(AnnexChangeValues values) {
		return reader.apply(values) != null;
	}
}
