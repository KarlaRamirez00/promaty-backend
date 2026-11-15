package com.promaty.rrhh.services.shared.sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.repository.AnnexRepository;
import com.promaty.rrhh.repository.ClientRepository;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.NonRehireReasonRepository;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.repository.ProjectSpecialtyRepository;
import com.promaty.rrhh.repository.ProjectTypeRepository;
import com.promaty.rrhh.repository.RequestRejectionReasonRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.services.annex.business.builder.AnnexSortField;
import com.promaty.rrhh.services.client.business.builder.ClientSortField;
import com.promaty.rrhh.services.contract.business.builder.ContractSortField;
import com.promaty.rrhh.services.nonrehirereason.business.builder.NonRehireReasonSortField;
import com.promaty.rrhh.services.project.business.builder.ProjectSortField;
import com.promaty.rrhh.services.projectspecialty.business.builder.ProjectSpecialtySortField;
import com.promaty.rrhh.services.projecttype.business.builder.ProjectTypeSortField;
import com.promaty.rrhh.services.request.business.builder.RequestSortField;
import com.promaty.rrhh.services.request.business.mapper.RequestTypeLabelResolver;
import com.promaty.rrhh.services.requestrejectionreason.business.builder.RequestRejectionReasonSortField;
import com.promaty.rrhh.services.staff.business.builder.StaffSortField;
import com.promaty.rrhh.services.termination.business.builder.TerminationSortField;

@SpringBootTest
class ListSortingIntegrationTest {

	@Autowired
	private ProjectTypeRepository projectTypeRepository;
	@Autowired
	private ClientRepository clientRepository;
	@Autowired
	private ProjectSpecialtyRepository projectSpecialtyRepository;
	@Autowired
	private RequestRejectionReasonRepository requestRejectionReasonRepository;
	@Autowired
	private NonRehireReasonRepository nonRehireReasonRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private ContractRepository contractRepository;
	@Autowired
	private TerminationRepository terminationRepository;
	@Autowired
	private AnnexRepository annexRepository;
	@Autowired
	private StaffRepository staffRepository;
	@Autowired
	private RequestRepository requestRepository;

	@TestFactory
	Stream<DynamicTest> everySortKeyRunsAgainstTheRealDatabase() {
		return Stream.of(
			sortCases(projectTypeRepository, ProjectTypeSortField.class),
			sortCases(clientRepository, ClientSortField.class),
			sortCases(projectSpecialtyRepository, ProjectSpecialtySortField.class),
			sortCases(requestRejectionReasonRepository, RequestRejectionReasonSortField.class),
			sortCases(nonRehireReasonRepository, NonRehireReasonSortField.class),
			sortCases(projectRepository, ProjectSortField.class),
			sortCases(contractRepository, ContractSortField.class),
			sortCases(terminationRepository, TerminationSortField.class),
			sortCases(annexRepository, AnnexSortField.class),
			sortCases(staffRepository, StaffSortField.class),
			sortCases(requestRepository, RequestSortField.class)
		).flatMap(Function.identity());
	}

	@Test
	void sortingByOptionalRelation_keepsRowsWithoutIt() {
		Specification<Annex> noFilters = (root, query, cb) -> null;
		SortedQuery<Annex> sorted = ListSorting.apply(noFilters, Pageable.unpaged(Sort.by("newJobTitleName")), AnnexSortField.class);

		assertThat(annexRepository.findAll(sorted.specification(), sorted.pageable()).getContent())
			.hasSize((int) annexRepository.count());
	}

	@Test
	void sortingRequestsByTypeName_followsTheLabelShownToTheUser() {
		Specification<Request> noFilters = (root, query, cb) -> null;
		SortedQuery<Request> sorted = ListSorting.apply(noFilters, Pageable.unpaged(Sort.by("typeName")), RequestSortField.class);

		List<String> labels = requestRepository.findAll(sorted.specification(), sorted.pageable()).stream()
			.map(request -> RequestTypeLabelResolver.resolve(request.getEntityType(), request.getAction()))
			.toList();

		assertThat(labels).isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);
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
