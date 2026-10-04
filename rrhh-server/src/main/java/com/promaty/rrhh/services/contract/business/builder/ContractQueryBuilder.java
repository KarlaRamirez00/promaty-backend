package com.promaty.rrhh.services.contract.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.contract.ContractFilterParams;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.services.shared.SearchNormalizer;

import jakarta.persistence.criteria.Predicate;

public final class ContractQueryBuilder {

	private ContractQueryBuilder() {
	}

	public static Specification<Contract> fromFilters(ContractFilterParams filters) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
				String unaccentedPattern = SearchNormalizer.likePattern(filters.getSearch());
				String plainPattern = "%" + filters.getSearch().toLowerCase() + "%";
				Predicate porNombre = cb.like(SearchNormalizer.unaccent(cb, cb.lower(root.get("name"))), unaccentedPattern);
				Predicate porRut = cb.like(cb.lower(root.get("staff").get("identificationNumber")), plainPattern);
				predicates.add(cb.or(porNombre, porRut));
			}
			if (filters.getProjectId() != null) {
				predicates.add(cb.equal(root.get("project").get("id"), filters.getProjectId()));
			}
			if (filters.getStatusId() != null) {
				predicates.add(cb.equal(root.get("status").get("id"), filters.getStatusId()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
