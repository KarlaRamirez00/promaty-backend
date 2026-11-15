package com.promaty.user.services.shared.sort;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.promaty.user.repository.RoleRepository;
import com.promaty.user.repository.UserRepository;
import com.promaty.user.services.role.business.builder.RoleSortField;
import com.promaty.user.services.user.business.builder.UserSortField;
import com.promaty.user.support.TestInternalApi;

@SpringBootTest(properties = "internal-api.secret=" + TestInternalApi.SECRET)
class ListSortingIntegrationTest {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;

	@TestFactory
	Stream<DynamicTest> everySortKeyRunsAgainstTheRealDatabase() {
		return Stream.of(
			sortCases(userRepository, UserSortField.class),
			sortCases(roleRepository, RoleSortField.class)
		).flatMap(Function.identity());
	}

	private <T, F extends Enum<F> & SortableField<T>> Stream<DynamicTest> sortCases(
		JpaSpecificationExecutor<T> repository,
		Class<F> fieldType
	) {
		Specification<T> noFilters = (root, query, cb) -> null;
		return Arrays.stream(fieldType.getEnumConstants()).map(field -> dynamicTest(
			fieldType.getSimpleName() + "." + field.key(),
			() -> {
				// Segunda pagina de tamano 1: Spring siempre ejecuta tambien el count, asi se valida esa consulta.
				Pageable pageable = PageRequest.of(1, 1, Sort.by(Sort.Direction.ASC, field.key()));
				SortedQuery<T> sorted = ListSorting.apply(noFilters, pageable, fieldType);
				assertThatNoException().isThrownBy(() -> repository.findAll(sorted.specification(), sorted.pageable()));
			}
		));
	}
}
