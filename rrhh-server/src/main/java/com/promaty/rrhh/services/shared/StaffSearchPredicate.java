package com.promaty.rrhh.services.shared;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public final class StaffSearchPredicate {

	private StaffSearchPredicate() {
	}

	public static Predicate matchesNameOrRut(CriteriaBuilder cb, Path<?> staff, String search) {
		String unaccentedPattern = SearchNormalizer.likePattern(search);
		String rutPattern = "%" + SearchNormalizer.withoutRutPunctuation(search) + "%";
		Predicate byName = cb.or(
			cb.like(SearchNormalizer.unaccent(cb, cb.lower(staff.get("firstName"))), unaccentedPattern),
			cb.like(SearchNormalizer.unaccent(cb, cb.lower(staff.get("paternalLastName"))), unaccentedPattern),
			cb.like(SearchNormalizer.unaccent(cb, cb.lower(staff.get("maternalLastName"))), unaccentedPattern)
		);
		Predicate byRut = cb.like(
			SearchNormalizer.withoutRutPunctuation(cb, cb.lower(staff.get("identificationNumber"))),
			rutPattern
		);
		return cb.or(byName, byRut);
	}
}
