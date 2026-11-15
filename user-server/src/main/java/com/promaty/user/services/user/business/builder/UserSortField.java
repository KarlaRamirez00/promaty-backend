package com.promaty.user.services.user.business.builder;

import java.util.List;

import com.promaty.user.entity.User;
import com.promaty.user.services.shared.sort.SortExpression;
import com.promaty.user.services.shared.sort.SortableField;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

public enum UserSortField implements SortableField<User> {

	ID("id"),
	FIRST_NAME("firstName"),
	LAST_NAME("lastName"),
	EMAIL("email"),
	ACTIVE("active"),
	ROLE_NAME("roleName", SortExpression.path("role", "name")),
	CREATED_AT("createdAt"),
	UPDATED_AT("updatedAt"),
	CREATED_BY("createdBy"),
	UPDATED_BY("updatedBy");

	private final String key;
	private final SortExpression<User> expression;

	UserSortField(String attribute) {
		this(attribute, SortExpression.attribute(attribute));
	}

	UserSortField(String key, SortExpression<User> expression) {
		this.key = key;
		this.expression = expression;
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public List<Expression<?>> expressions(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		return expression.resolve(root, query, cb);
	}
}
