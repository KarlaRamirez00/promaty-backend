package com.promaty.rrhh.services.staff.business.builder;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.staff.StaffFilterParams;
import com.promaty.rrhh.entity.Staff;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public final class StaffQueryBuilder {

	private static final Pattern MARCAS_DIACRITICAS = Pattern.compile("\\p{M}");

	private StaffQueryBuilder() {
	}

	public static Specification<Staff> fromFilters(StaffFilterParams filters) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
				String patron = "%" + sinTildes(filters.getSearch().toLowerCase()) + "%";
				predicates.add(cb.or(
					cb.like(sinTildesSql(cb, cb.lower(root.get("firstName"))), patron),
					cb.like(sinTildesSql(cb, cb.lower(root.get("paternalLastName"))), patron),
					cb.like(sinTildesSql(cb, cb.lower(root.get("maternalLastName"))), patron),
					cb.like(cb.lower(root.get("identificationNumber")), "%" + filters.getSearch().toLowerCase() + "%")
				));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	private static Expression<String> sinTildesSql(CriteriaBuilder cb, Expression<String> texto) {
		return cb.function("unaccent", String.class, texto);
	}

	private static String sinTildes(String texto) {
		return MARCAS_DIACRITICAS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
	}
}
