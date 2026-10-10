package com.promaty.rrhh.services.request.business.builder;

import java.time.LocalDateTime;
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
			if (filters.getRequesterUserId() != null) {
				predicates.add(cb.equal(root.get("requesterUserId"), filters.getRequesterUserId()));
			}
			if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
				String patron = "%" + filters.getSearch().toLowerCase() + "%";
				predicates.add(cb.or(
					cb.like(cb.lower(root.get("project").get("name")), patron),
					cb.like(cb.lower(root.get("project").get("costCenterCode")), patron)
				));
			}
			if (filters.getCreatedFrom() != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filters.getCreatedFrom().atStartOfDay()));
			}
			if (filters.getCreatedTo() != null) {
				LocalDateTime finDelDia = filters.getCreatedTo().atTime(23, 59, 59);
				predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), finDelDia));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
