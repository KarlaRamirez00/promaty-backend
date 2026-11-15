package com.promaty.rrhh.services.termination.business.builder;

import java.util.List;

import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.services.shared.sort.SortExpression;
import com.promaty.rrhh.services.shared.sort.SortableField;
import com.promaty.rrhh.services.shared.sort.StaffSortPaths;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

public enum TerminationSortField implements SortableField<Termination> {

	ID("id"),
	STAFF_NAME("staffName", (root, query, cb) -> StaffSortPaths.fullName(root.get("staff"))),
	STAFF_IDENTIFICATION_NUMBER("staffIdentificationNumber", (root, query, cb) -> StaffSortPaths.identificationNumber(root.get("staff"))),
	COST_CENTER_CODE("costCenterCode", SortExpression.path("project", "costCenterCode")),
	LEGAL_TERMINATION_CAUSE_NAME("legalTerminationCauseName", SortExpression.path("legalTerminationCause", "name")),
	TERMINATION_DATE("terminationDate"),
	DEFINITIVE_TERMINATION("definitiveTermination"),
	STATUS_NAME("statusName", SortExpression.path("status", "name")),
	CREATED_AT("createdAt"),
	UPDATED_AT("updatedAt"),
	CREATED_BY("createdBy"),
	UPDATED_BY("updatedBy");

	private final String key;
	private final SortExpression<Termination> expression;

	TerminationSortField(String attribute) {
		this(attribute, SortExpression.attribute(attribute));
	}

	TerminationSortField(String key, SortExpression<Termination> expression) {
		this.key = key;
		this.expression = expression;
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public List<Expression<?>> expressions(Root<Termination> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		return expression.resolve(root, query, cb);
	}
}
