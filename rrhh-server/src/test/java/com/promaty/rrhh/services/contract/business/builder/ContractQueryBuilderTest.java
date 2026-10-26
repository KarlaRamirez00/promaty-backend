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
	void fromFilters_conSearch_agregaPredicadoDeNombreDeColaboradorORut() {
		ContractFilterParams filtros = new ContractFilterParams();
		filtros.setSearch("Perez");

		Path<Object> staffPath = mock(Path.class);
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
		Expression<String> puntoLiteral = mock(Expression.class);
		Expression<String> guionLiteral = mock(Expression.class);
		Expression<String> vacioLiteral = mock(Expression.class);
		Expression<String> idNumberSinPuntos = mock(Expression.class);
		Expression<String> idNumberSinPuntosNiGuion = mock(Expression.class);
		Predicate firstNamePredicate = mock(Predicate.class);
		Predicate paternalPredicate = mock(Predicate.class);
		Predicate maternalPredicate = mock(Predicate.class);
		Predicate rutPredicate = mock(Predicate.class);
		Predicate namePredicate = mock(Predicate.class);
		Predicate orPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);

		when(root.<Object>get("staff")).thenReturn(staffPath);
		when(staffPath.<String>get("firstName")).thenReturn(firstNamePath);
		when(staffPath.<String>get("paternalLastName")).thenReturn(paternalPath);
		when(staffPath.<String>get("maternalLastName")).thenReturn(maternalPath);
		when(staffPath.<String>get("identificationNumber")).thenReturn(idNumberPath);
		when(cb.lower(firstNamePath)).thenReturn(firstNameLower);
		when(cb.lower(paternalPath)).thenReturn(paternalLower);
		when(cb.lower(maternalPath)).thenReturn(maternalLower);
		when(cb.lower(idNumberPath)).thenReturn(idNumberLower);
		when(cb.function("unaccent", String.class, firstNameLower)).thenReturn(firstNameNoAccent);
		when(cb.function("unaccent", String.class, paternalLower)).thenReturn(paternalNoAccent);
		when(cb.function("unaccent", String.class, maternalLower)).thenReturn(maternalNoAccent);
		when(cb.like(firstNameNoAccent, "%perez%")).thenReturn(firstNamePredicate);
		when(cb.like(paternalNoAccent, "%perez%")).thenReturn(paternalPredicate);
		when(cb.like(maternalNoAccent, "%perez%")).thenReturn(maternalPredicate);
		when(cb.<String>literal(".")).thenReturn(puntoLiteral);
		when(cb.<String>literal("-")).thenReturn(guionLiteral);
		when(cb.<String>literal("")).thenReturn(vacioLiteral);
		when(cb.function("replace", String.class, idNumberLower, puntoLiteral, vacioLiteral)).thenReturn(idNumberSinPuntos);
		when(cb.function("replace", String.class, idNumberSinPuntos, guionLiteral, vacioLiteral)).thenReturn(idNumberSinPuntosNiGuion);
		when(cb.like(idNumberSinPuntosNiGuion, "%perez%")).thenReturn(rutPredicate);
		when(cb.or(firstNamePredicate, paternalPredicate, maternalPredicate)).thenReturn(namePredicate);
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

	@Test
	void fromFilters_conContractTypeId_agregaPredicadoDeIgualdad() {
		ContractFilterParams filtros = new ContractFilterParams();
		filtros.setContractTypeId(2L);

		Path<Object> contractTypePath = mock(Path.class);
		Path<Long> contractTypeIdPath = mock(Path.class);
		Predicate contractTypePredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<Object>get("contractType")).thenReturn(contractTypePath);
		when(contractTypePath.<Long>get("id")).thenReturn(contractTypeIdPath);
		when(cb.equal(contractTypeIdPath, 2L)).thenReturn(contractTypePredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = ContractQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(contractTypePredicate);
	}
}
