package com.promaty.rrhh.services.project.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.promaty.rrhh.dto.project.ProjectFilterParams;
import com.promaty.rrhh.entity.Project;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@SuppressWarnings("unchecked")
class ProjectQueryBuilderTest {

	private final Root<Project> root = mock(Root.class);
	private final CriteriaQuery<?> query = mock(CriteriaQuery.class);
	private final CriteriaBuilder cb = mock(CriteriaBuilder.class);

	@Test
	void fromFilters_sinFiltros_noAgregaPredicados() {
		ProjectFilterParams filtros = new ProjectFilterParams();
		Predicate combinado = mock(Predicate.class);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ProjectQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).isEmpty();
		verifyNoInteractions(root);
	}

	@Test
	void fromFilters_conSearch_agregaPredicadoDeNombreOCentroCosto() {
		ProjectFilterParams filtros = new ProjectFilterParams();
		filtros.setSearch("Norte");

		Path<String> namePath = mock(Path.class);
		Path<String> costCenterPath = mock(Path.class);
		Expression<String> nameLower = mock(Expression.class);
		Expression<String> costCenterLower = mock(Expression.class);
		Predicate namePredicate = mock(Predicate.class);
		Predicate costCenterPredicate = mock(Predicate.class);
		Predicate orPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<String>get("name")).thenReturn(namePath);
		when(root.<String>get("costCenterCode")).thenReturn(costCenterPath);
		when(cb.lower(namePath)).thenReturn(nameLower);
		when(cb.lower(costCenterPath)).thenReturn(costCenterLower);
		when(cb.like(nameLower, "%norte%")).thenReturn(namePredicate);
		when(cb.like(costCenterLower, "%norte%")).thenReturn(costCenterPredicate);
		when(cb.or(namePredicate, costCenterPredicate)).thenReturn(orPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ProjectQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(orPredicate);
	}

	@Test
	void fromFilters_conTypeId_agregaPredicadoDeIgualdadSobreLaFk() {
		ProjectFilterParams filtros = new ProjectFilterParams();
		filtros.setTypeId(10L);

		Path<Object> typePath = mock(Path.class);
		Path<Long> typeIdPath = mock(Path.class);
		Predicate typePredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<Object>get("type")).thenReturn(typePath);
		when(typePath.<Long>get("id")).thenReturn(typeIdPath);
		when(cb.equal(typeIdPath, 10L)).thenReturn(typePredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ProjectQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(typePredicate);
	}

	@Test
	void fromFilters_conSpecialtyId_agregaPredicadoDeIgualdadSobreLaFk() {
		ProjectFilterParams filtros = new ProjectFilterParams();
		filtros.setSpecialtyId(20L);

		Path<Object> specialtyPath = mock(Path.class);
		Path<Long> specialtyIdPath = mock(Path.class);
		Predicate specialtyPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<Object>get("specialty")).thenReturn(specialtyPath);
		when(specialtyPath.<Long>get("id")).thenReturn(specialtyIdPath);
		when(cb.equal(specialtyIdPath, 20L)).thenReturn(specialtyPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ProjectQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(specialtyPredicate);
	}

	@Test
	void fromFilters_conClientId_agregaPredicadoDeIgualdadSobreLaFk() {
		ProjectFilterParams filtros = new ProjectFilterParams();
		filtros.setClientId(30L);

		Path<Object> clientPath = mock(Path.class);
		Path<Long> clientIdPath = mock(Path.class);
		Predicate clientPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<Object>get("client")).thenReturn(clientPath);
		when(clientPath.<Long>get("id")).thenReturn(clientIdPath);
		when(cb.equal(clientIdPath, 30L)).thenReturn(clientPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ProjectQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(clientPredicate);
	}

	@Test
	void fromFilters_conStatusId_agregaPredicadoDeIgualdadSobreLaFk() {
		ProjectFilterParams filtros = new ProjectFilterParams();
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

		Predicate resultado = ProjectQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(statusPredicate);
	}

	@Test
	void fromFilters_conStartDateFrom_agregaPredicadoMayorOIgual() {
		ProjectFilterParams filtros = new ProjectFilterParams();
		filtros.setStartDateFrom(LocalDate.of(2026, 1, 1));

		Path<LocalDate> startDatePath = mock(Path.class);
		Predicate startDatePredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<LocalDate>get("startDate")).thenReturn(startDatePath);
		when(cb.greaterThanOrEqualTo(startDatePath, LocalDate.of(2026, 1, 1))).thenReturn(startDatePredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ProjectQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(startDatePredicate);
	}

	@Test
	void fromFilters_conStartDateTo_agregaPredicadoMenorOIgual() {
		ProjectFilterParams filtros = new ProjectFilterParams();
		filtros.setStartDateTo(LocalDate.of(2026, 6, 30));

		Path<LocalDate> startDatePath = mock(Path.class);
		Predicate startDatePredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<LocalDate>get("startDate")).thenReturn(startDatePath);
		when(cb.lessThanOrEqualTo(startDatePath, LocalDate.of(2026, 6, 30))).thenReturn(startDatePredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ProjectQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(startDatePredicate);
	}

	@Test
	void fromFilters_conSearchYTypeId_combinaAmbosPredicados() {
		ProjectFilterParams filtros = new ProjectFilterParams();
		filtros.setSearch("Norte");
		filtros.setTypeId(10L);

		Path<String> namePath = mock(Path.class);
		Path<String> costCenterPath = mock(Path.class);
		Expression<String> nameLower = mock(Expression.class);
		Expression<String> costCenterLower = mock(Expression.class);
		Predicate namePredicate = mock(Predicate.class);
		Predicate costCenterPredicate = mock(Predicate.class);
		Predicate orPredicate = mock(Predicate.class);
		Path<Object> typePath = mock(Path.class);
		Path<Long> typeIdPath = mock(Path.class);
		Predicate typePredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<String>get("name")).thenReturn(namePath);
		when(root.<String>get("costCenterCode")).thenReturn(costCenterPath);
		when(cb.lower(namePath)).thenReturn(nameLower);
		when(cb.lower(costCenterPath)).thenReturn(costCenterLower);
		when(cb.like(nameLower, "%norte%")).thenReturn(namePredicate);
		when(cb.like(costCenterLower, "%norte%")).thenReturn(costCenterPredicate);
		when(cb.or(namePredicate, costCenterPredicate)).thenReturn(orPredicate);
		when(root.<Object>get("type")).thenReturn(typePath);
		when(typePath.<Long>get("id")).thenReturn(typeIdPath);
		when(cb.equal(typeIdPath, 10L)).thenReturn(typePredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ProjectQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(orPredicate, typePredicate);
	}
}
