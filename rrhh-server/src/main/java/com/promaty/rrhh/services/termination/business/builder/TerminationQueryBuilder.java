package com.promaty.rrhh.services.termination.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.termination.TerminationFilterParams;
import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.services.shared.SearchNormalizer;

import jakarta.persistence.criteria.Predicate;

public final class TerminationQueryBuilder {

	private TerminationQueryBuilder() {
	}

	public static Specification<Termination> fromFilters(TerminationFilterParams filters) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
				String unaccentedPattern = SearchNormalizer.likePattern(filters.getSearch());
				String rutPattern = "%" + SearchNormalizer.withoutRutPunctuation(filters.getSearch()) + "%";
				var staff = root.get("staff");
				Predicate porNombre = cb.or(
					cb.like(SearchNormalizer.unaccent(cb, cb.lower(staff.get("firstName"))), unaccentedPattern),
					cb.like(SearchNormalizer.unaccent(cb, cb.lower(staff.get("paternalLastName"))), unaccentedPattern),
					cb.like(SearchNormalizer.unaccent(cb, cb.lower(staff.get("maternalLastName"))), unaccentedPattern)
				);
				Predicate porRut = cb.like(
					SearchNormalizer.withoutRutPunctuation(cb, cb.lower(staff.get("identificationNumber"))),
					rutPattern
				);
				predicates.add(cb.or(porNombre, porRut));
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
