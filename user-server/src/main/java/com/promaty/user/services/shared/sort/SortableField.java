package com.promaty.user.services.shared.sort;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

public interface SortableField<T> {

	String key();

	List<Expression<?>> expressions(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
