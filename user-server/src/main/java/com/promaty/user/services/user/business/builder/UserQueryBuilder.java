package com.promaty.user.services.user.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.user.dto.user.UserFilterParams;
import com.promaty.user.entity.User;

import jakarta.persistence.criteria.Predicate;

public final class UserQueryBuilder {

	private UserQueryBuilder() {
	}

	public static Specification<User> fromFilters(UserFilterParams filters) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filters.getRoleId() != null) {
				predicates.add(cb.equal(root.get("role").get("id"), filters.getRoleId()));
			}
			if (filters.getActive() != null) {
				predicates.add(cb.equal(root.get("active"), filters.getActive()));
			}
			if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
				String patron = "%" + filters.getSearch().toLowerCase() + "%";
				predicates.add(cb.or(
					cb.like(cb.lower(root.get("firstName")), patron),
					cb.like(cb.lower(root.get("lastName")), patron),
					cb.like(cb.lower(root.get("email")), patron)
				));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
