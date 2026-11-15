package com.promaty.rrhh.services.staff.business.builder;

import java.time.LocalDate;
import java.util.List;

import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.services.shared.sort.SortExpression;
import com.promaty.rrhh.services.shared.sort.SortableField;
import com.promaty.rrhh.services.shared.sort.StaffSortPaths;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public enum StaffSortField implements SortableField<Staff> {

	ID("id"),
	IDENTIFICATION_TYPE("identificationType"),
	IDENTIFICATION_NUMBER("identificationNumber"),
	STAFF_NAME("staffName", (root, query, cb) -> StaffSortPaths.fullName(root)),
	FIRST_NAME("firstName"),
	PATERNAL_LAST_NAME("paternalLastName"),
	MATERNAL_LAST_NAME("maternalLastName"),
	COST_CENTER_CODE("costCenterCode", StaffSortField::activeContractCostCenterCode),
	HIRE_DATE("hireDate", StaffSortField::latestEverActiveContractStartDate),
	STATUS_NAME("statusName", SortExpression.path("status", "name")),
	CREATED_AT("createdAt"),
	UPDATED_AT("updatedAt"),
	CREATED_BY("createdBy"),
	UPDATED_BY("updatedBy");

	private static final String STAFF = "staff";
	private static final String STATUS = "status";
	private static final String CODE = "code";

	private final String key;
	private final SortExpression<Staff> expression;

	StaffSortField(String attribute) {
		this(attribute, SortExpression.attribute(attribute));
	}

	StaffSortField(String key, SortExpression<Staff> expression) {
		this.key = key;
		this.expression = expression;
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public List<Expression<?>> expressions(Root<Staff> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		return expression.resolve(root, query, cb);
	}

	private static List<Expression<?>> activeContractCostCenterCode(Root<Staff> staff, CriteriaQuery<?> query, CriteriaBuilder cb) {
		Subquery<String> subquery = query.subquery(String.class);
		Root<Contract> contract = subquery.from(Contract.class);
		subquery.select(cb.greatest(contract.get("project").<String>get("costCenterCode")))
			.where(
				cb.equal(contract.get(STAFF), staff),
				cb.equal(contract.get(STATUS).get(CODE), Contract.STATUS_CODE_ACTIVE)
			);
		return List.of(subquery);
	}

	private static List<Expression<?>> latestEverActiveContractStartDate(Root<Staff> staff, CriteriaQuery<?> query, CriteriaBuilder cb) {
		Subquery<LocalDate> subquery = query.subquery(LocalDate.class);
		Root<Contract> contract = subquery.from(Contract.class);
		subquery.select(cb.greatest(contract.<LocalDate>get("startDate")))
			.where(
				cb.equal(contract.get(STAFF), staff),
				contract.get(STATUS).get(CODE).in(Contract.STATUS_CODES_EVER_ACTIVE)
			);
		return List.of(subquery);
	}
}
