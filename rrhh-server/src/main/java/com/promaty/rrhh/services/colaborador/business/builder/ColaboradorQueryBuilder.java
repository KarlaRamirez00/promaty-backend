package com.promaty.rrhh.services.colaborador.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.colaborador.ColaboradorFilterParams;
import com.promaty.rrhh.entity.Colaborador;

import jakarta.persistence.criteria.Predicate;

public final class ColaboradorQueryBuilder {

	private ColaboradorQueryBuilder() {
	}

	public static Specification<Colaborador> fromFilters(ColaboradorFilterParams filters) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filters.getActive() != null) {
				predicates.add(cb.equal(root.get("active"), filters.getActive()));
			}
			if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
				String patron = "%" + filters.getSearch().toLowerCase() + "%";
				predicates.add(cb.or(
					cb.like(cb.lower(root.get("firstName")), patron),
					cb.like(cb.lower(root.get("paternalLastName")), patron),
					cb.like(cb.lower(root.get("maternalLastName")), patron),
					cb.like(cb.lower(root.get("identificationNumber")), patron)
				));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
