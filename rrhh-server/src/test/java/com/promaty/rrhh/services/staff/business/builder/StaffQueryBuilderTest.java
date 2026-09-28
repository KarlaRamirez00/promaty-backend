package com.promaty.rrhh.services.staff.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.promaty.rrhh.dto.staff.StaffFilterParams;
import com.promaty.rrhh.entity.Staff;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@SuppressWarnings("unchecked")
class StaffQueryBuilderTest {

	private final Root<Staff> root = mock(Root.class);
	private final CriteriaQuery<?> query = mock(CriteriaQuery.class);
	private final CriteriaBuilder cb = mock(CriteriaBuilder.class);

	@Test
	void fromFilters_sinFiltros_noAgregaPredicados() {
		StaffFilterParams filtros = new StaffFilterParams();
		Predicate combinado = mock(Predicate.class);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = StaffQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).isEmpty();
		verifyNoInteractions(root);
	}

	@Test
	void fromFilters_conSearchConTilde_normalizaElTerminoYUsaUnaccentEnLasColumnas() {
		StaffFilterParams filtros = new StaffFilterParams();
		filtros.setSearch("Ramír");

		Path<String> firstNamePath = mock(Path.class);
		Path<String> paternalPath = mock(Path.class);
		Path<String> maternalPath = mock(Path.class);
		Path<String> idNumberPath = mock(Path.class);
		Expression<String> firstNameLower = mock(Expression.class);
		Expression<String> paternalLower = mock(Expression.class);
		Expression<String> maternalLower = mock(Expression.class);
		Expression<String> idNumberLower = mock(Expression.class);
		Expression<String> firstNameNoAccent = mock(Expression.class);
		Expression<String> paternalNoAccent = mock(Expression.class);
		Expression<String> maternalNoAccent = mock(Expression.class);
		Predicate firstNamePredicate = mock(Predicate.class);
		Predicate paternalPredicate = mock(Predicate.class);
		Predicate maternalPredicate = mock(Predicate.class);
		Predicate idNumberPredicate = mock(Predicate.class);
		Predicate orPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);

		when(root.<String>get("firstName")).thenReturn(firstNamePath);
		when(root.<String>get("paternalLastName")).thenReturn(paternalPath);
		when(root.<String>get("maternalLastName")).thenReturn(maternalPath);
		when(root.<String>get("identificationNumber")).thenReturn(idNumberPath);
		when(cb.lower(firstNamePath)).thenReturn(firstNameLower);
		when(cb.lower(paternalPath)).thenReturn(paternalLower);
		when(cb.lower(maternalPath)).thenReturn(maternalLower);
		when(cb.lower(idNumberPath)).thenReturn(idNumberLower);
		when(cb.function("unaccent", String.class, firstNameLower)).thenReturn(firstNameNoAccent);
		when(cb.function("unaccent", String.class, paternalLower)).thenReturn(paternalNoAccent);
		when(cb.function("unaccent", String.class, maternalLower)).thenReturn(maternalNoAccent);
		when(cb.like(firstNameNoAccent, "%ramir%")).thenReturn(firstNamePredicate);
		when(cb.like(paternalNoAccent, "%ramir%")).thenReturn(paternalPredicate);
		when(cb.like(maternalNoAccent, "%ramir%")).thenReturn(maternalPredicate);
		when(cb.like(idNumberLower, "%ramír%")).thenReturn(idNumberPredicate);
		when(cb.or(firstNamePredicate, paternalPredicate, maternalPredicate, idNumberPredicate))
			.thenReturn(orPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = StaffQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(orPredicate);
	}
}
