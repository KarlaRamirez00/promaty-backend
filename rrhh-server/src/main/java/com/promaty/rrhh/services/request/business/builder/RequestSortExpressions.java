package com.promaty.rrhh.services.request.business.builder;

import java.util.function.Function;

import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestAction;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.services.request.business.mapper.RequestTypeLabelResolver;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

final class RequestSortExpressions {

	private static final String ENTITY_TYPE = "entityType";
	private static final String ACTION = "action";
	private static final String ENTITY_ID = "entityId";
	private static final String ID = "id";
	private static final String STAFF = "staff";
	private static final String CONTRACT = "contract";
	private static final String JOB_TITLE = "jobTitle";
	private static final String NAME = "name";

	private RequestSortExpressions() {
	}

	static <Y> Expression<Y> staffAttribute(Root<Request> request, CriteriaQuery<?> query, CriteriaBuilder cb, Class<Y> type, String attribute) {
		return byEntityType(request, query, cb, type, entityType -> new String[] {STAFF, attribute});
	}

	static Expression<String> staffJobTitleName(Root<Request> request, CriteriaQuery<?> query, CriteriaBuilder cb) {
		return byEntityType(request, query, cb, String.class, RequestSortExpressions::jobTitleNamePath);
	}

	static Expression<String> typeLabel(Root<Request> request, CriteriaBuilder cb) {
		CriteriaBuilder.Case<String> label = cb.selectCase();
		for (RequestEntityType entityType : RequestEntityType.values()) {
			for (RequestAction action : RequestAction.values()) {
				label = label.when(
					cb.and(cb.equal(request.get(ENTITY_TYPE), entityType), cb.equal(request.get(ACTION), action)),
					RequestTypeLabelResolver.resolve(entityType, action)
				);
			}
		}
		return label.otherwise(cb.nullLiteral(String.class));
	}

	private static <Y> Expression<Y> byEntityType(
		Root<Request> request,
		CriteriaQuery<?> query,
		CriteriaBuilder cb,
		Class<Y> type,
		Function<RequestEntityType, String[]> pathFromEntity
	) {
		CriteriaBuilder.SimpleCase<RequestEntityType, Y> byType = cb.selectCase(request.<RequestEntityType>get(ENTITY_TYPE));
		for (RequestEntityType entityType : RequestEntityType.values()) {
			byType = byType.when(entityType, valueFromEntity(request, query, cb, type, entityType, pathFromEntity.apply(entityType)));
		}
		return byType.otherwise(cb.nullLiteral(type));
	}

	private static <Y> Subquery<Y> valueFromEntity(
		Root<Request> request,
		CriteriaQuery<?> query,
		CriteriaBuilder cb,
		Class<Y> type,
		RequestEntityType entityType,
		String[] path
	) {
		Subquery<Y> subquery = query.subquery(type);
		Root<?> entity = subquery.from(entityClass(entityType));
		subquery.select(navigate(entity, path))
			.where(cb.equal(entity.get(ID), request.get(ENTITY_ID)));
		return subquery;
	}

	private static Class<?> entityClass(RequestEntityType entityType) {
		return switch (entityType) {
			case CONTRACT -> Contract.class;
			case TERMINATION -> Termination.class;
			case ANNEX -> Annex.class;
		};
	}

	private static String[] jobTitleNamePath(RequestEntityType entityType) {
		return switch (entityType) {
			case CONTRACT -> new String[] {JOB_TITLE, NAME};
			case TERMINATION, ANNEX -> new String[] {CONTRACT, JOB_TITLE, NAME};
		};
	}

	@SuppressWarnings("unchecked")
	private static <Y> Path<Y> navigate(Path<?> from, String[] path) {
		Path<?> current = from;
		for (String segment : path) {
			current = current.get(segment);
		}
		return (Path<Y>) current;
	}
}
