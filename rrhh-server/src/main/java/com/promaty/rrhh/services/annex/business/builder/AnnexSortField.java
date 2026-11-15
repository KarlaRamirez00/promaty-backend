package com.promaty.rrhh.services.annex.business.builder;

import java.util.List;

import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.services.shared.sort.SortExpression;
import com.promaty.rrhh.services.shared.sort.SortableField;
import com.promaty.rrhh.services.shared.sort.StaffSortPaths;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

public enum AnnexSortField implements SortableField<Annex> {

	ID("id"),
	STAFF_NAME("staffName", (root, query, cb) -> StaffSortPaths.fullName(root.get("staff"))),
	STAFF_IDENTIFICATION_NUMBER("staffIdentificationNumber", (root, query, cb) -> StaffSortPaths.identificationNumber(root.get("staff"))),
	COST_CENTER_CODE("costCenterCode", SortExpression.path("project", "costCenterCode")),
	ANNEX_TYPE_NAME("annexTypeName", SortExpression.path("annexType", "name")),
	NEW_JOB_TITLE_NAME("newJobTitleName", SortExpression.leftJoin("newJobTitle", "name")),
	NEW_BASE_SALARY("newBaseSalary"),
	NEW_END_DATE("newEndDate"),
	EFFECTIVE_DATE("effectiveDate"),
	STATUS_NAME("statusName", SortExpression.path("status", "name")),
	CREATED_AT("createdAt"),
	UPDATED_AT("updatedAt"),
	CREATED_BY("createdBy"),
	UPDATED_BY("updatedBy");

	private final String key;
	private final SortExpression<Annex> expression;

	AnnexSortField(String attribute) {
		this(attribute, SortExpression.attribute(attribute));
	}

	AnnexSortField(String key, SortExpression<Annex> expression) {
		this.key = key;
		this.expression = expression;
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public List<Expression<?>> expressions(Root<Annex> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		return expression.resolve(root, query, cb);
	}
}
