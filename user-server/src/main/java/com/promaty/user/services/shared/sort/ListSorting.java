package com.promaty.user.services.shared.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.promaty.user.exception.BusinessValidationException;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

public final class ListSorting {

	private static final String FIELD_SORT = "sort";
	private static final String TIE_BREAKER_KEY = "id";
	private static final String VALIDATION_MESSAGE = "La validacion fallo para uno o mas campos.";

	private ListSorting() {
	}

	public static <T, F extends Enum<F> & SortableField<T>> SortedQuery<T> apply(
		Specification<T> specification,
		Pageable pageable,
		Class<F> fieldType
	) {
		List<FieldOrder<T>> orders = resolveOrders(pageable.getSort(), fieldType);
		Specification<T> ordering = (root, query, cb) -> {
			if (!isCountQuery(query)) {
				query.orderBy(toJpaOrders(orders, root, query, cb));
			}
			return cb.conjunction();
		};
		return new SortedQuery<>(specification.and(ordering), withoutSort(pageable));
	}

	private static <T, F extends Enum<F> & SortableField<T>> List<FieldOrder<T>> resolveOrders(Sort sort, Class<F> fieldType) {
		Map<String, F> fieldsByKey = Arrays.stream(fieldType.getEnumConstants())
			.collect(Collectors.toMap(SortableField::key, field -> field));
		F tieBreaker = Optional.ofNullable(fieldsByKey.get(TIE_BREAKER_KEY))
			.orElseThrow(() -> new IllegalStateException(fieldType.getSimpleName() + " debe declarar la clave '" + TIE_BREAKER_KEY + "'."));

		List<FieldOrder<T>> orders = new ArrayList<>();
		for (Sort.Order order : sort) {
			F field = Optional.ofNullable(fieldsByKey.get(order.getProperty()))
				.orElseThrow(() -> invalidSort(order.getProperty(), fieldsByKey.keySet()));
			orders.add(new FieldOrder<>(field, order.isAscending()));
		}
		boolean hasTieBreaker = orders.stream().anyMatch(order -> order.field() == tieBreaker);
		if (!hasTieBreaker) {
			orders.add(new FieldOrder<>(tieBreaker, false));
		}
		return orders;
	}

	// Spring Data reutiliza la Specification para el count de la paginacion; un ORDER BY ahi no aporta y Postgres lo rechaza.
	private static boolean isCountQuery(CriteriaQuery<?> query) {
		return Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType());
	}

	private static <T> List<Order> toJpaOrders(List<FieldOrder<T>> orders, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		List<Order> jpaOrders = new ArrayList<>();
		for (FieldOrder<T> order : orders) {
			order.field().expressions(root, query, cb).forEach(expression ->
				jpaOrders.add(order.ascending() ? cb.asc(expression) : cb.desc(expression))
			);
		}
		return jpaOrders;
	}

	private static Pageable withoutSort(Pageable pageable) {
		return pageable.isPaged() ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()) : Pageable.unpaged();
	}

	private static BusinessValidationException invalidSort(String key, Set<String> allowedKeys) {
		String allowed = allowedKeys.stream().sorted().collect(Collectors.joining(", "));
		return new BusinessValidationException(
			VALIDATION_MESSAGE,
			Map.of(FIELD_SORT, "No se puede ordenar por '" + key + "'. Valores permitidos: " + allowed + ".")
		);
	}

	private record FieldOrder<T>(SortableField<T> field, boolean ascending) {
	}
}
