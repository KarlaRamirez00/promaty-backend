package com.promaty.rrhh.services.nonrehirereason.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonFilterParams;
import com.promaty.rrhh.entity.NonRehireReason;

import jakarta.persistence.criteria.Predicate;

public final class NonRehireReasonQueryBuilder {

	private NonRehireReasonQueryBuilder() {
	}

	public static Specification<NonRehireReason> fromFilters(NonRehireReasonFilterParams filters) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filters.getActive() != null) {
				predicates.add(cb.equal(root.get("active"), filters.getActive()));
			}
			if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
				String patron = "%" + filters.getSearch().toLowerCase() + "%";
				predicates.add(cb.or(
					cb.like(cb.lower(root.get("name")), patron),
					cb.like(cb.lower(root.get("code")), patron)
				));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
