package com.promaty.rrhh.services.staff.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.promaty.rrhh.dto.staff.StaffFilterParams;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Staff;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

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
		Expression<String> puntoLiteral = mock(Expression.class);
		Expression<String> guionLiteral = mock(Expression.class);
		Expression<String> vacioLiteral = mock(Expression.class);
		Expression<String> idNumberSinPuntos = mock(Expression.class);
		Expression<String> idNumberSinPuntosNiGuion = mock(Expression.class);
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
		when(cb.<String>literal(".")).thenReturn(puntoLiteral);
		when(cb.<String>literal("-")).thenReturn(guionLiteral);
		when(cb.<String>literal("")).thenReturn(vacioLiteral);
		when(cb.function("replace", String.class, idNumberLower, puntoLiteral, vacioLiteral)).thenReturn(idNumberSinPuntos);
		when(cb.function("replace", String.class, idNumberSinPuntos, guionLiteral, vacioLiteral)).thenReturn(idNumberSinPuntosNiGuion);
		when(cb.like(firstNameNoAccent, "%ramir%")).thenReturn(firstNamePredicate);
		when(cb.like(paternalNoAccent, "%ramir%")).thenReturn(paternalPredicate);
		when(cb.like(maternalNoAccent, "%ramir%")).thenReturn(maternalPredicate);
		when(cb.like(idNumberSinPuntosNiGuion, "%ramír%")).thenReturn(idNumberPredicate);
		when(cb.or(firstNamePredicate, paternalPredicate, maternalPredicate, idNumberPredicate))
			.thenReturn(orPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = StaffQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(orPredicate);
	}

	@Test
	void fromFilters_conStatusCode_filtraPorCodigoDeEstado() {
		StaffFilterParams filtros = new StaffFilterParams();
		filtros.setStatusCode("ACTIVE");

		Path<Object> statusPath = mock(Path.class);
		Path<String> codePath = mock(Path.class);
		Predicate statusPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);

		when(root.get("status")).thenReturn(statusPath);
		when(statusPath.<String>get("code")).thenReturn(codePath);
		when(cb.equal(codePath, "ACTIVE")).thenReturn(statusPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = StaffQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(statusPredicate);
	}

	@Test
	void fromFilters_conCostCenterCode_filtraPorSubqueryDeContratoActivo() {
		StaffFilterParams filtros = new StaffFilterParams();
		filtros.setCostCenterCode("00824");

		Subquery<Long> subquery = mock(Subquery.class);
		Root<Contract> contractRoot = mock(Root.class);
		Path<Object> contractStaffPath = mock(Path.class);
		Path<Long> contractStaffIdPath = mock(Path.class);
		Path<Object> contractStatusPath = mock(Path.class);
		Path<String> contractStatusCodePath = mock(Path.class);
		Path<Object> contractProjectPath = mock(Path.class);
		Path<String> contractCostCenterCodePath = mock(Path.class);
		Predicate statusEquals = mock(Predicate.class);
		Predicate costCenterEquals = mock(Predicate.class);
		Path<Long> staffIdPath = mock(Path.class);
		Predicate inPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);

		when(query.subquery(Long.class)).thenReturn(subquery);
		when(subquery.from(Contract.class)).thenReturn(contractRoot);
		when(contractRoot.get("staff")).thenReturn(contractStaffPath);
		when(contractStaffPath.<Long>get("id")).thenReturn(contractStaffIdPath);
		when(contractRoot.get("status")).thenReturn(contractStatusPath);
		when(contractStatusPath.<String>get("code")).thenReturn(contractStatusCodePath);
		when(contractRoot.get("project")).thenReturn(contractProjectPath);
		when(contractProjectPath.<String>get("costCenterCode")).thenReturn(contractCostCenterCodePath);
		when(cb.equal(contractStatusCodePath, "ACTIVE")).thenReturn(statusEquals);
		when(cb.equal(contractCostCenterCodePath, "00824")).thenReturn(costCenterEquals);
		when(subquery.select(contractStaffIdPath)).thenReturn(subquery);
		when(subquery.where(statusEquals, costCenterEquals)).thenReturn(subquery);
		when(root.<Long>get("id")).thenReturn(staffIdPath);
		when(staffIdPath.in(subquery)).thenReturn(inPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = StaffQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(inPredicate);
	}
}
