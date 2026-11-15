package com.promaty.rrhh.services.projecttype.business.builder;

import java.util.List;

import com.promaty.rrhh.entity.ProjectType;
import com.promaty.rrhh.services.shared.sort.SortExpression;
import com.promaty.rrhh.services.shared.sort.SortableField;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

public enum ProjectTypeSortField implements SortableField<ProjectType> {

	ID("id"),
	NAME("name"),
	ACTIVE("active"),
	CREATED_AT("createdAt"),
	UPDATED_AT("updatedAt"),
	CREATED_BY("createdBy"),
	UPDATED_BY("updatedBy");

	private final String key;
	private final SortExpression<ProjectType> expression;

	ProjectTypeSortField(String attribute) {
		this.key = attribute;
		this.expression = SortExpression.attribute(attribute);
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public List<Expression<?>> expressions(Root<ProjectType> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		return expression.resolve(root, query, cb);
	}
}
