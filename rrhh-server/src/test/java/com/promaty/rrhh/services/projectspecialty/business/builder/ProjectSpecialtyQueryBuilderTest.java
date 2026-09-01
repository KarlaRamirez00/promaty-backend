package com.promaty.rrhh.services.projectspecialty.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyFilterParams;
import com.promaty.rrhh.entity.ProjectSpecialty;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@SuppressWarnings("unchecked")
class ProjectSpecialtyQueryBuilderTest {

	private final Root<ProjectSpecialty> root = mock(Root.class);
	private final CriteriaQuery<?> query = mock(CriteriaQuery.class);
	private final CriteriaBuilder cb = mock(CriteriaBuilder.class);

	@Test
	void fromFilters_sinFiltros_noAgregaPredicados() {
		ProjectSpecialtyFilterParams filtros = new ProjectSpecialtyFilterParams();
		Predicate combinado = mock(Predicate.class);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ProjectSpecialtyQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).isEmpty();
		verifyNoInteractions(root);
	}

	@Test
	void fromFilters_conActive_agregaPredicadoDeIgualdad() {
		ProjectSpecialtyFilterParams filtros = new ProjectSpecialtyFilterParams();
		filtros.setActive(true);

		Path<Boolean> activePath = mock(Path.class);
		Predicate activePredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<Boolean>get("active")).thenReturn(activePath);
		when(cb.equal(activePath, true)).thenReturn(activePredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ProjectSpecialtyQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(activePredicate);
	}

	@Test
	void fromFilters_conSearch_agregaPredicadoLikeSobreName() {
		ProjectSpecialtyFilterParams filtros = new ProjectSpecialtyFilterParams();
		filtros.setSearch("Eléc");

		Path<String> namePath = mock(Path.class);
		Expression<String> nameLower = mock(Expression.class);
		Predicate likePredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<String>get("name")).thenReturn(namePath);
		when(cb.lower(namePath)).thenReturn(nameLower);
		when(cb.like(nameLower, "%eléc%")).thenReturn(likePredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ProjectSpecialtyQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(likePredicate);
	}

	@Test
	void fromFilters_conSearchEnBlanco_noAgregaPredicado() {
		ProjectSpecialtyFilterParams filtros = new ProjectSpecialtyFilterParams();
		filtros.setSearch("   ");
		Predicate combinado = mock(Predicate.class);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		ProjectSpecialtyQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(captor.getValue()).isEmpty();
	}

	@Test
	void fromFilters_conActiveYSearch_combinaAmbosPredicados() {
		ProjectSpecialtyFilterParams filtros = new ProjectSpecialtyFilterParams();
		filtros.setActive(false);
		filtros.setSearch("Eléc");

		Path<Boolean> activePath = mock(Path.class);
		Predicate activePredicate = mock(Predicate.class);
		Path<String> namePath = mock(Path.class);
		Expression<String> nameLower = mock(Expression.class);
		Predicate likePredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<Boolean>get("active")).thenReturn(activePath);
		when(cb.equal(activePath, false)).thenReturn(activePredicate);
		when(root.<String>get("name")).thenReturn(namePath);
		when(cb.lower(namePath)).thenReturn(nameLower);
		when(cb.like(nameLower, "%eléc%")).thenReturn(likePredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ProjectSpecialtyQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(activePredicate, likePredicate);
	}
}
