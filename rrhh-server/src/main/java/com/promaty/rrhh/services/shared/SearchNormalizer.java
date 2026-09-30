package com.promaty.rrhh.services.shared;

import java.text.Normalizer;
import java.util.regex.Pattern;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;

/**
 * Ignora tildes al filtrar por texto: el patrón se limpia en Java (Normalizer), la columna se limpia
 * con unaccent() de Postgres (requiere CREATE EXTENSION unaccent, ver data.sql) — así "ramir"
 * encuentra "Ramírez" sin que el usuario tenga que escribir el acento exacto.
 */
public final class SearchNormalizer {

	private static final Pattern DIACRITIC_MARKS = Pattern.compile("\\p{M}");

	private SearchNormalizer() {
	}

	public static Expression<String> unaccent(CriteriaBuilder cb, Expression<String> text) {
		return cb.function("unaccent", String.class, text);
	}

	public static String likePattern(String term) {
		String normalized = Normalizer.normalize(term.toLowerCase(), Normalizer.Form.NFD);
		return "%" + DIACRITIC_MARKS.matcher(normalized).replaceAll("") + "%";
	}
}
