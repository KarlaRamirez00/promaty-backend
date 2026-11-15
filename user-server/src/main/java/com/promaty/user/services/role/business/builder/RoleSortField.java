package com.promaty.user.services.role.business.builder;

import java.util.List;

import com.promaty.user.entity.Role;
import com.promaty.user.entity.User;
import com.promaty.user.services.shared.sort.SortExpression;
import com.promaty.user.services.shared.sort.SortableField;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public enum RoleSortField implements SortableField<Role> {

	ID("id"),
	NAME("name"),
	DESCRIPTION("description"),
	ACTIVE("active"),
	TOTAL_USERS("totalUsers", RoleSortField::userCount),
	CREATED_AT("createdAt"),
	UPDATED_AT("updatedAt"),
	CREATED_BY("createdBy"),
	UPDATED_BY("updatedBy");

	private final String key;
	private final SortExpression<Role> expression;

	RoleSortField(String attribute) {
		this(attribute, SortExpression.attribute(attribute));
	}

	RoleSortField(String key, SortExpression<Role> expression) {
		this.key = key;
		this.expression = expression;
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public List<Expression<?>> expressions(Root<Role> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		return expression.resolve(root, query, cb);
	}

	private static List<Expression<?>> userCount(Root<Role> role, CriteriaQuery<?> query, CriteriaBuilder cb) {
		Subquery<Long> subquery = query.subquery(Long.class);
		Root<User> user = subquery.from(User.class);
		subquery.select(cb.count(user)).where(cb.equal(user.get("role"), role));
		return List.of(subquery);
	}
}
