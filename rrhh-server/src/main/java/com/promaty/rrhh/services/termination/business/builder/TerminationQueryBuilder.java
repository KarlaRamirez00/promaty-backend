package com.promaty.rrhh.services.termination.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.termination.TerminationFilterParams;
import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.services.shared.StaffSearchPredicate;

import jakarta.persistence.criteria.Predicate;

public final class TerminationQueryBuilder {

	private TerminationQueryBuilder() {
	}

	public static Specification<Termination> fromFilters(TerminationFilterParams filters) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
				predicates.add(StaffSearchPredicate.matchesNameOrRut(cb, root.get("staff"), filters.getSearch()));
			}
			if (filters.getProjectId() != null) {
				predicates.add(cb.equal(root.get("project").get("id"), filters.getProjectId()));
			}
			if (filters.getStatusId() != null) {
				predicates.add(cb.equal(root.get("status").get("id"), filters.getStatusId()));
			}
			if (filters.getTerminationDateFrom() != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("terminationDate"), filters.getTerminationDateFrom()));
			}
			if (filters.getTerminationDateTo() != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("terminationDate"), filters.getTerminationDateTo()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
