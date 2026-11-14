package com.promaty.rrhh.services.annex.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.annex.AnnexFilterParams;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.services.shared.StaffSearchPredicate;

import jakarta.persistence.criteria.Predicate;

public final class AnnexQueryBuilder {

	private static final String FIELD_EFFECTIVE_DATE = "effectiveDate";

	private AnnexQueryBuilder() {
	}

	public static Specification<Annex> fromFilters(AnnexFilterParams filters) {
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
			if (filters.getAnnexTypeId() != null) {
				predicates.add(cb.equal(root.get("annexType").get("id"), filters.getAnnexTypeId()));
			}
			if (filters.getEffectiveDateFrom() != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get(FIELD_EFFECTIVE_DATE), filters.getEffectiveDateFrom()));
			}
			if (filters.getEffectiveDateTo() != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_EFFECTIVE_DATE), filters.getEffectiveDateTo()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
