package com.promaty.rrhh.services.contract.business.builder;

import java.util.List;

import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.services.shared.sort.SortExpression;
import com.promaty.rrhh.services.shared.sort.SortableField;
import com.promaty.rrhh.services.shared.sort.StaffSortPaths;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

public enum ContractSortField implements SortableField<Contract> {

	ID("id"),
	STAFF_NAME("staffName", (root, query, cb) -> StaffSortPaths.fullName(root.get("staff"))),
	STAFF_IDENTIFICATION_NUMBER("staffIdentificationNumber", (root, query, cb) -> StaffSortPaths.identificationNumber(root.get("staff"))),
	COST_CENTER_CODE("costCenterCode", SortExpression.path("project", "costCenterCode")),
	CONTRACT_TYPE_NAME("contractTypeName", SortExpression.path("contractType", "name")),
	JOB_TITLE_NAME("jobTitleName", SortExpression.path("jobTitle", "name")),
	STATUS_NAME("statusName", SortExpression.path("status", "name")),
	START_DATE("startDate"),
	END_DATE("endDate"),
	AGREED_SALARY("agreedSalary"),
	CREATED_AT("createdAt"),
	UPDATED_AT("updatedAt"),
	CREATED_BY("createdBy"),
	UPDATED_BY("updatedBy");

	private final String key;
	private final SortExpression<Contract> expression;

	ContractSortField(String attribute) {
		this(attribute, SortExpression.attribute(attribute));
	}

	ContractSortField(String key, SortExpression<Contract> expression) {
		this.key = key;
		this.expression = expression;
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public List<Expression<?>> expressions(Root<Contract> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		return expression.resolve(root, query, cb);
	}
}
