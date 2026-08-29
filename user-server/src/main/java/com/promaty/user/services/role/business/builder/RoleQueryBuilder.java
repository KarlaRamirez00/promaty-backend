package com.promaty.user.services.role.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.user.dto.role.RoleFilterParams;
import com.promaty.user.entity.Role;

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
				String patron = "%" + filters.getSearch().toLowerCase() + "%";
				predicates.add(cb.or(
					cb.like(cb.lower(root.get("name")), patron),
					cb.like(cb.lower(root.get("description")), patron)
				));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
