package com.promaty.rrhh.services.nonrehirereason.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonFilterParams;
import com.promaty.rrhh.entity.NonRehireReason;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@SuppressWarnings("unchecked")
class NonRehireReasonQueryBuilderTest {

	private final Root<NonRehireReason> root = mock(Root.class);
	private final CriteriaQuery<?> query = mock(CriteriaQuery.class);
	private final CriteriaBuilder cb = mock(CriteriaBuilder.class);

	@Test
	void fromFilters_sinFiltros_noAgregaPredicados() {
		NonRehireReasonFilterParams filtros = new NonRehireReasonFilterParams();
		Predicate combinado = mock(Predicate.class);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = NonRehireReasonQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).isEmpty();
		verifyNoInteractions(root);
	}

	@Test
	void fromFilters_conActive_agregaPredicadoDeIgualdad() {
		NonRehireReasonFilterParams filtros = new NonRehireReasonFilterParams();
		filtros.setActive(true);

		Path<Boolean> activePath = mock(Path.class);
		Predicate activePredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<Boolean>get("active")).thenReturn(activePath);
		when(cb.equal(activePath, true)).thenReturn(activePredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = NonRehireReasonQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(activePredicate);
	}

	@Test
	void fromFilters_conSearch_agregaPredicadoOrSobreNameYCode() {
		NonRehireReasonFilterParams filtros = new NonRehireReasonFilterParams();
		filtros.setSearch("bajo");

		Path<String> namePath = mock(Path.class);
		Path<String> codePath = mock(Path.class);
		Expression<String> nameLower = mock(Expression.class);
		Expression<String> codeLower = mock(Expression.class);
		Predicate namePredicate = mock(Predicate.class);
		Predicate codePredicate = mock(Predicate.class);
		Predicate orPredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<String>get("name")).thenReturn(namePath);
		when(root.<String>get("code")).thenReturn(codePath);
		when(cb.lower(namePath)).thenReturn(nameLower);
		when(cb.lower(codePath)).thenReturn(codeLower);
		when(cb.like(nameLower, "%bajo%")).thenReturn(namePredicate);
		when(cb.like(codeLower, "%bajo%")).thenReturn(codePredicate);
		when(cb.or(namePredicate, codePredicate)).thenReturn(orPredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = NonRehireReasonQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(orPredicate);
	}

	@Test
	void fromFilters_conSearchEnBlanco_noAgregaPredicado() {
		NonRehireReasonFilterParams filtros = new NonRehireReasonFilterParams();
		filtros.setSearch("   ");
		Predicate combinado = mock(Predicate.class);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		NonRehireReasonQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(captor.getValue()).isEmpty();
	}
}
