package com.promaty.user.services.shared.sort;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

@FunctionalInterface
public interface SortExpression<T> {

	List<Expression<?>> resolve(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);

	static <T> SortExpression<T> attribute(String name) {
		return path(name);
	}

	static <T> SortExpression<T> path(String... segments) {
		return (root, query, cb) -> {
			Path<?> current = root;
			for (String segment : segments) {
				current = current.get(segment);
			}
			return List.of(current);
		};
	}

	static <T> SortExpression<T> leftJoin(String optionalRelation, String attribute) {
		return (root, query, cb) -> List.of(root.join(optionalRelation, JoinType.LEFT).get(attribute));
	}
}
