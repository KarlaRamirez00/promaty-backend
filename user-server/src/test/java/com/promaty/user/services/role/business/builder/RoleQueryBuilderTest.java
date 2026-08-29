package com.promaty.user.services.role.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.domain.Specification;

import com.promaty.user.dto.role.RoleFilterParams;
import com.promaty.user.entity.Role;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@SuppressWarnings("unchecked")
class RoleQueryBuilderTest {

	private final Root<Role> root = mock(Root.class);
	private final CriteriaQuery<?> query = mock(CriteriaQuery.class);
	private final CriteriaBuilder cb = mock(CriteriaBuilder.class);

	@Test
	void fromFilters_sinFiltros_noAgregaPredicados() {
		RoleFilterParams filtros = new RoleFilterParams();
		Predicate combinado = mock(Predicate.class);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = RoleQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).isEmpty();
		verifyNoInteractions(root);
	}

	@Test
	void fromFilters_conActive_agregaPredicadoDeIgualdad() {
		RoleFilterParams filtros = new RoleFilterParams();
		filtros.setActive(true);

		Path<Boolean> activePath = mock(Path.class);
		Predicate activePredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<Boolean>get("active")).thenReturn(activePath);
		when(cb.equal(activePath, true)).thenReturn(activePredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = RoleQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(activePredicate);
	}

	@Test
	void fromFilters_conSearch_agregaPredicadoDeNombreODescripcion() {
		RoleFilterParams filtros = new RoleFilterParams();
		filtros.setSearch("Admin");

		Path<String> namePath = mock(Path.class);
		Path<String> descPath = mock(Path.class);
		Expression<String> nameLower = mock(Expression.class);
		Expression<String> descLower = mock(Expression.class);
		Predicate namePredicate = mock(Predicate.class);
		Predicate descPredicate = mock(Predicate.class);
		Predicate orPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);

		when(root.<String>get("name")).thenReturn(namePath);
		when(root.<String>get("description")).thenReturn(descPath);
		when(cb.lower(namePath)).thenReturn(nameLower);
		when(cb.lower(descPath)).thenReturn(descLower);
		when(cb.like(nameLower, "%admin%")).thenReturn(namePredicate);
		when(cb.like(descLower, "%admin%")).thenReturn(descPredicate);
		when(cb.or(namePredicate, descPredicate)).thenReturn(orPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = RoleQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(orPredicate);
	}

	@Test
	void fromFilters_conActiveYSearch_combinaAmbosPredicados() {
		RoleFilterParams filtros = new RoleFilterParams();
		filtros.setActive(false);
		filtros.setSearch("Admin");

		Path<Boolean> activePath = mock(Path.class);
		Predicate activePredicate = mock(Predicate.class);
		Path<String> namePath = mock(Path.class);
		Path<String> descPath = mock(Path.class);
		Expression<String> nameLower = mock(Expression.class);
		Expression<String> descLower = mock(Expression.class);
		Predicate namePredicate = mock(Predicate.class);
		Predicate descPredicate = mock(Predicate.class);
		Predicate orPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);

		when(root.<Boolean>get("active")).thenReturn(activePath);
		when(cb.equal(activePath, false)).thenReturn(activePredicate);
		when(root.<String>get("name")).thenReturn(namePath);
		when(root.<String>get("description")).thenReturn(descPath);
		when(cb.lower(namePath)).thenReturn(nameLower);
		when(cb.lower(descPath)).thenReturn(descLower);
		when(cb.like(nameLower, "%admin%")).thenReturn(namePredicate);
		when(cb.like(descLower, "%admin%")).thenReturn(descPredicate);
		when(cb.or(namePredicate, descPredicate)).thenReturn(orPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = RoleQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(activePredicate, orPredicate);
	}
}
