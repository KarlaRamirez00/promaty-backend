package com.promaty.rrhh.services.request.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.request.RequestFilterParams;
import com.promaty.rrhh.entity.Request;

import jakarta.persistence.criteria.Predicate;

public final class RequestQueryBuilder {

	private RequestQueryBuilder() {
	}

	public static Specification<Request> fromFilters(RequestFilterParams filters) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filters.getEntityType() != null) {
				predicates.add(cb.equal(root.get("entityType"), filters.getEntityType()));
			}
			if (filters.getProjectId() != null) {
				predicates.add(cb.equal(root.get("project").get("id"), filters.getProjectId()));
			}
			if (filters.getStatusId() != null) {
				predicates.add(cb.equal(root.get("status").get("id"), filters.getStatusId()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
