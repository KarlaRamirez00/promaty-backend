package com.promaty.rrhh.services.project.business.builder;

import java.util.List;

import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.services.shared.sort.SortExpression;
import com.promaty.rrhh.services.shared.sort.SortableField;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

public enum ProjectSortField implements SortableField<Project> {

	ID("id"),
	NAME("name"),
	COST_CENTER_CODE("costCenterCode"),
	TYPE_NAME("typeName", SortExpression.path("type", "name")),
	SPECIALTY_NAME("specialtyName", SortExpression.path("specialty", "name")),
	CLIENT_NAME("clientName", SortExpression.path("client", "name")),
	STATUS_NAME("statusName", SortExpression.path("status", "name")),
	START_DATE("startDate"),
	END_DATE("endDate"),
	CREATED_AT("createdAt"),
	UPDATED_AT("updatedAt"),
	CREATED_BY("createdBy"),
	UPDATED_BY("updatedBy");

	private final String key;
	private final SortExpression<Project> expression;

	ProjectSortField(String attribute) {
		this(attribute, SortExpression.attribute(attribute));
	}

	ProjectSortField(String key, SortExpression<Project> expression) {
		this.key = key;
		this.expression = expression;
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public List<Expression<?>> expressions(Root<Project> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		return expression.resolve(root, query, cb);
	}
}
