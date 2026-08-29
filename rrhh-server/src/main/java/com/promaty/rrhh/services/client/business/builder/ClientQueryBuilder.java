package com.promaty.rrhh.services.client.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.client.ClientFilterParams;
import com.promaty.rrhh.entity.Client;

import jakarta.persistence.criteria.Predicate;

public final class ClientQueryBuilder {

	private ClientQueryBuilder() {
	}

	public static Specification<Client> fromFilters(ClientFilterParams filters) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filters.getActive() != null) {
				predicates.add(cb.equal(root.get("active"), filters.getActive()));
			}
			if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
				String patron = "%" + filters.getSearch().toLowerCase() + "%";
				predicates.add(cb.like(cb.lower(root.get("name")), patron));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
