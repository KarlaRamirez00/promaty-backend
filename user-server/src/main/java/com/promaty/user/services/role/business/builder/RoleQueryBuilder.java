package com.promaty.user.services.role.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.user.dto.role.RoleFilterParams;
import com.promaty.user.entity.Role;
import com.promaty.user.services.shared.SearchNormalizer;

import jakarta.persistence.criteria.Predicate;

public final class RoleQueryBuilder {

	private RoleQueryBuilder() {
	}

	public static Specification<Role> fromFilters(RoleFilterParams filters) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filters.getActive() != null) {
				predicates.add(cb.equal(root.get("active"), filters.getActive()));
			}
			if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
				String pattern = SearchNormalizer.likePattern(filters.getSearch());
				predicates.add(cb.or(
					cb.like(SearchNormalizer.unaccent(cb, cb.lower(root.get("name"))), pattern),
					cb.like(SearchNormalizer.unaccent(cb, cb.lower(root.get("description"))), pattern)
				));
			}
			if (filters.getSubModuleId() != null) {
				predicates.add(cb.equal(root.join("subModules").get("id"), filters.getSubModuleId()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
