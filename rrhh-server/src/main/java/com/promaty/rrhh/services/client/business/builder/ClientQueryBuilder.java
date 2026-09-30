package com.promaty.rrhh.services.client.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.client.ClientFilterParams;
import com.promaty.rrhh.entity.Client;
import com.promaty.rrhh.services.shared.SearchNormalizer;

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
				String pattern = SearchNormalizer.likePattern(filters.getSearch());
				predicates.add(cb.like(SearchNormalizer.unaccent(cb, cb.lower(root.get("name"))), pattern));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
