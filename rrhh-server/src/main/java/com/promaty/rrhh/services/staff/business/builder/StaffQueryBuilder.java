package com.promaty.rrhh.services.staff.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.staff.StaffFilterParams;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.services.shared.SearchNormalizer;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;

public final class StaffQueryBuilder {

	private static final String CODIGO_ESTADO_CONTRATO_ACTIVO = "ACTIVE";

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
					cb.like(
						SearchNormalizer.withoutRutPunctuation(cb, cb.lower(root.get("identificationNumber"))),
						"%" + SearchNormalizer.withoutRutPunctuation(filters.getSearch()) + "%"
					)
				));
			}

			if (filters.getStatusCode() != null && !filters.getStatusCode().isBlank()) {
				predicates.add(cb.equal(root.get("status").get("code"), filters.getStatusCode()));
			}

			if (filters.getCostCenterCode() != null && !filters.getCostCenterCode().isBlank()) {
				Subquery<Long> contratoActivoDelCentroDeCosto = query.subquery(Long.class);
				var contract = contratoActivoDelCentroDeCosto.from(Contract.class);
				contratoActivoDelCentroDeCosto.select(contract.get("staff").get("id"))
					.where(
						cb.equal(contract.get("status").get("code"), CODIGO_ESTADO_CONTRATO_ACTIVO),
						cb.equal(contract.get("project").get("costCenterCode"), filters.getCostCenterCode())
					);
				predicates.add(root.get("id").in(contratoActivoDelCentroDeCosto));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
