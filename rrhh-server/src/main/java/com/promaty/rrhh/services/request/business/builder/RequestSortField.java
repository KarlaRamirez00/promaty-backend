package com.promaty.rrhh.services.request.business.builder;

import java.util.List;

import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.services.shared.sort.SortExpression;
import com.promaty.rrhh.services.shared.sort.SortableField;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

public enum RequestSortField implements SortableField<Request> {

	ID("id"),
	TYPE_NAME("typeName", (root, query, cb) -> List.of(RequestSortExpressions.typeLabel(root, cb))),
	PROJECT_NAME("projectName", SortExpression.path("project", "name")),
	COST_CENTER_CODE("costCenterCode", SortExpression.path("project", "costCenterCode")),
	REQUESTER_NAME("requesterName", SortExpression.attribute("createdBy")),
	STAFF_NAME("staffName", (root, query, cb) -> List.of(
		RequestSortExpressions.staffAttribute(root, query, cb, String.class, "firstName"),
		RequestSortExpressions.staffAttribute(root, query, cb, String.class, "paternalLastName"),
		RequestSortExpressions.staffAttribute(root, query, cb, String.class, "maternalLastName")
	)),
	STAFF_IDENTIFICATION_TYPE("staffIdentificationType", (root, query, cb) -> List.of(
		RequestSortExpressions.staffAttribute(root, query, cb, IdentificationType.class, "identificationType")
	)),
	STAFF_IDENTIFICATION_NUMBER("staffIdentificationNumber", (root, query, cb) -> List.of(
		RequestSortExpressions.staffAttribute(root, query, cb, String.class, "identificationNumber")
	)),
	STAFF_POSITION("staffPosition", (root, query, cb) -> List.of(RequestSortExpressions.staffJobTitleName(root, query, cb))),
	STATUS_NAME("statusName", SortExpression.path("status", "name")),
	CREATED_AT("createdAt"),
	UPDATED_AT("updatedAt"),
	CREATED_BY("createdBy"),
	UPDATED_BY("updatedBy");

	private final String key;
	private final SortExpression<Request> expression;

	RequestSortField(String attribute) {
		this(attribute, SortExpression.attribute(attribute));
	}

	RequestSortField(String key, SortExpression<Request> expression) {
		this.key = key;
		this.expression = expression;
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public List<Expression<?>> expressions(Root<Request> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		return expression.resolve(root, query, cb);
	}
}
