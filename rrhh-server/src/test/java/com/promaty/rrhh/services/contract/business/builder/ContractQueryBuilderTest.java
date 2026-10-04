package com.promaty.rrhh.services.contract.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.promaty.rrhh.dto.contract.ContractFilterParams;
import com.promaty.rrhh.entity.Contract;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@SuppressWarnings("unchecked")
class ContractQueryBuilderTest {

	private final Root<Contract> root = mock(Root.class);
	private final CriteriaQuery<?> query = mock(CriteriaQuery.class);
	private final CriteriaBuilder cb = mock(CriteriaBuilder.class);

	@Test
	void fromFilters_sinFiltros_noAgregaPredicados() {
		ContractFilterParams filtros = new ContractFilterParams();
		Predicate combinado = mock(Predicate.class);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ContractQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).isEmpty();
		verifyNoInteractions(root);
	}

	@Test
	void fromFilters_conSearch_agregaPredicadoDeNombreORut() {
		ContractFilterParams filtros = new ContractFilterParams();
		filtros.setSearch("Perez");

		Path<String> namePath = mock(Path.class);
		Expression<String> nameLower = mock(Expression.class);
		Expression<String> nameNoAccent = mock(Expression.class);
		Path<Object> staffPath = mock(Path.class);
		Path<String> idNumberPath = mock(Path.class);
		Expression<String> idNumberLower = mock(Expression.class);
		Predicate namePredicate = mock(Predicate.class);
		Predicate rutPredicate = mock(Predicate.class);
		Predicate orPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);

		when(root.<String>get("name")).thenReturn(namePath);
		when(cb.lower(namePath)).thenReturn(nameLower);
		when(cb.function("unaccent", String.class, nameLower)).thenReturn(nameNoAccent);
		when(root.<Object>get("staff")).thenReturn(staffPath);
		when(staffPath.<String>get("identificationNumber")).thenReturn(idNumberPath);
		when(cb.lower(idNumberPath)).thenReturn(idNumberLower);
		when(cb.like(nameNoAccent, "%perez%")).thenReturn(namePredicate);
		when(cb.like(idNumberLower, "%perez%")).thenReturn(rutPredicate);
		when(cb.or(namePredicate, rutPredicate)).thenReturn(orPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ContractQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(orPredicate);
	}

	@Test
	void fromFilters_conProjectId_agregaPredicadoDeIgualdad() {
		ContractFilterParams filtros = new ContractFilterParams();
		filtros.setProjectId(10L);

		Path<Object> projectPath = mock(Path.class);
		Path<Long> projectIdPath = mock(Path.class);
		Predicate projectPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<Object>get("project")).thenReturn(projectPath);
		when(projectPath.<Long>get("id")).thenReturn(projectIdPath);
		when(cb.equal(projectIdPath, 10L)).thenReturn(projectPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ContractQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(projectPredicate);
	}

	@Test
	void fromFilters_conStatusId_agregaPredicadoDeIgualdad() {
		ContractFilterParams filtros = new ContractFilterParams();
		filtros.setStatusId(40L);

		Path<Object> statusPath = mock(Path.class);
		Path<Long> statusIdPath = mock(Path.class);
		Predicate statusPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<Object>get("status")).thenReturn(statusPath);
		when(statusPath.<Long>get("id")).thenReturn(statusIdPath);
		when(cb.equal(statusIdPath, 40L)).thenReturn(statusPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ContractQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(statusPredicate);
	}
}
