package com.promaty.rrhh.services.staff.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.staff.StaffFilterParams;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.services.shared.SearchNormalizer;

import jakarta.persistence.criteria.Predicate;

public final class StaffQueryBuilder {

	private StaffQueryBuilder() {
	}

	public static Specification<Staff> fromFilters(StaffFilterParams filters) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
				String pattern = SearchNormalizer.likePattern(filters.getSearch());
				predicates.add(cb.or(
					cb.like(SearchNormalizer.unaccent(cb, cb.lower(root.get("firstName"))), pattern),
					cb.like(SearchNormalizer.unaccent(cb, cb.lower(root.get("paternalLastName"))), pattern),
					cb.like(SearchNormalizer.unaccent(cb, cb.lower(root.get("maternalLastName"))), pattern),
					cb.like(cb.lower(root.get("identificationNumber")), "%" + filters.getSearch().toLowerCase() + "%")
				));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
