package com.promaty.rrhh.services.termination.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.promaty.rrhh.dto.termination.TerminationFilterParams;
import com.promaty.rrhh.entity.Termination;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@SuppressWarnings("unchecked")
class TerminationQueryBuilderTest {

	private final Root<Termination> root = mock(Root.class);
	private final CriteriaQuery<?> query = mock(CriteriaQuery.class);
	private final CriteriaBuilder cb = mock(CriteriaBuilder.class);

	@Test
	void fromFilters_sinFiltros_noAgregaPredicados() {
		TerminationFilterParams filtros = new TerminationFilterParams();
		Predicate combinado = mock(Predicate.class);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = TerminationQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).isEmpty();
		verifyNoInteractions(root);
	}

	@Test
	void fromFilters_conProjectId_agregaPredicadoDeIgualdad() {
		TerminationFilterParams filtros = new TerminationFilterParams();
		filtros.setProjectId(5L);

		var projectPath = mock(Path.class);
		Path<Long> idPath = mock(Path.class);
		Predicate projectPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.get("project")).thenReturn(projectPath);
		when(projectPath.<Long>get("id")).thenReturn(idPath);
		when(cb.equal(idPath, 5L)).thenReturn(projectPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = TerminationQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(projectPredicate);
	}

	@Test
	void fromFilters_conStatusId_agregaPredicadoDeIgualdad() {
		TerminationFilterParams filtros = new TerminationFilterParams();
		filtros.setStatusId(3L);

		var statusPath = mock(Path.class);
		Path<Long> idPath = mock(Path.class);
		Predicate statusPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.get("status")).thenReturn(statusPath);
		when(statusPath.<Long>get("id")).thenReturn(idPath);
		when(cb.equal(idPath, 3L)).thenReturn(statusPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = TerminationQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(statusPredicate);
	}
}
