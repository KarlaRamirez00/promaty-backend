package com.promaty.user.services.shared.sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.promaty.user.exception.BusinessValidationException;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class ListSortingTest {

	private final Specification<Object> noFilters = (root, query, cb) -> null;

	@Mock
	private Root<Object> root;
	@Mock
	private CriteriaQuery<Object> query;
	@Mock
	private CriteriaBuilder cb;
	@Mock
	private Path<Object> idPath;
	@Mock
	private Path<Object> namePath;
	@Captor
	private ArgumentCaptor<List<Order>> ordersCaptor;

	@Test
	void invalidKey_throwsValidationErrorListingAllowedKeys() {
		Pageable pageable = PageRequest.of(0, 20, Sort.by("salary"));

		assertThatThrownBy(() -> ListSorting.apply(noFilters, pageable, FakeSortField.class))
			.isInstanceOfSatisfying(BusinessValidationException.class, ex -> assertThat(ex.getErrorFields())
				.containsEntry("sort", "No se puede ordenar por 'salary'. Valores permitidos: id, name."));
	}

	@Test
	void validKey_ordersByItAndBreaksTiesByIdDescending() {
		Order nameAsc = mock(Order.class);
		Order idDesc = mock(Order.class);
		doReturn(namePath).when(root).get("name");
		doReturn(idPath).when(root).get("id");
		when(cb.asc(namePath)).thenReturn(nameAsc);
		when(cb.desc(idPath)).thenReturn(idDesc);

		SortedQuery<Object> sorted = ListSorting.apply(noFilters, PageRequest.of(0, 20, Sort.by("name")), FakeSortField.class);
		sorted.specification().toPredicate(root, query, cb);

		verify(query).orderBy(ordersCaptor.capture());
		assertThat(ordersCaptor.getValue()).containsExactly(nameAsc, idDesc);
	}

	@Test
	void explicitIdKey_isNotAddedTwiceAsTieBreaker() {
		Order idAsc = mock(Order.class);
		doReturn(idPath).when(root).get("id");
		when(cb.asc(idPath)).thenReturn(idAsc);

		SortedQuery<Object> sorted = ListSorting.apply(noFilters, PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id")), FakeSortField.class);
		sorted.specification().toPredicate(root, query, cb);

		verify(query).orderBy(ordersCaptor.capture());
		assertThat(ordersCaptor.getValue()).containsExactly(idAsc);
	}

	@Test
	void countQuery_isNotOrdered() {
		doReturn(Long.class).when(query).getResultType();

		SortedQuery<Object> sorted = ListSorting.apply(noFilters, PageRequest.of(0, 20, Sort.by("name")), FakeSortField.class);
		sorted.specification().toPredicate(root, query, cb);

		verify(query, never()).orderBy(anyList());
	}

	@Test
	void pagedRequest_keepsPageAndSizeWithoutSort() {
		SortedQuery<Object> sorted = ListSorting.apply(noFilters, PageRequest.of(2, 10, Sort.by("name")), FakeSortField.class);

		assertThat(sorted.pageable()).isEqualTo(PageRequest.of(2, 10));
	}

	@Test
	void unpagedRequest_staysUnpaged() {
		SortedQuery<Object> sorted = ListSorting.apply(noFilters, Pageable.unpaged(), FakeSortField.class);

		assertThat(sorted.pageable().isUnpaged()).isTrue();
	}

	@Test
	void enumWithoutIdKey_failsFast() {
		Pageable pageable = PageRequest.of(0, 20);

		assertThatThrownBy(() -> ListSorting.apply(noFilters, pageable, WithoutIdSortField.class))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("WithoutIdSortField");
	}

	private enum FakeSortField implements SortableField<Object> {
		ID("id"),
		NAME("name");

		private final String key;

		FakeSortField(String key) {
			this.key = key;
		}

		@Override
		public String key() {
			return key;
		}

		@Override
		public List<Expression<?>> expressions(Root<Object> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
			return List.of(root.get(key));
		}
	}

	private enum WithoutIdSortField implements SortableField<Object> {
		NAME;

		@Override
		public String key() {
			return "name";
		}

		@Override
		public List<Expression<?>> expressions(Root<Object> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
			return List.of(root.get("name"));
		}
	}
}
