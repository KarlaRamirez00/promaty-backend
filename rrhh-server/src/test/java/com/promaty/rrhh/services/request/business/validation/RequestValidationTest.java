package com.promaty.rrhh.services.request.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.request.ContractPendingDataDto;
import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.request.TerminationPendingDataDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.ContractType;
import com.promaty.rrhh.entity.LegalTerminationCause;
import com.promaty.rrhh.entity.NonRehireReason;
import com.promaty.rrhh.entity.RequestAction;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.CompanyRepository;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.ContractTypeRepository;
import com.promaty.rrhh.repository.JobTitleRepository;
import com.promaty.rrhh.repository.LegalTerminationCauseRepository;
import com.promaty.rrhh.repository.MealTypeRepository;
import com.promaty.rrhh.repository.NonRehireReasonRepository;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.repository.SiteRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.repository.TransportTypeRepository;

@ExtendWith(MockitoExtension.class)
class RequestValidationTest {

	private static final Long PROJECT_ID = 1L;
	private static final Long STAFF_ID = 2L;
	private static final Long COMPANY_ID = 3L;
	private static final Long CONTRACT_TYPE_ID = 4L;
	private static final Long JOB_TITLE_ID = 5L;
	private static final Long SITE_ID = 6L;

	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private StaffRepository staffRepository;
	@Mock
	private CompanyRepository companyRepository;
	@Mock
	private ContractTypeRepository contractTypeRepository;
	@Mock
	private JobTitleRepository jobTitleRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private MealTypeRepository mealTypeRepository;
	@Mock
	private TransportTypeRepository transportTypeRepository;
	@Mock
	private ContractRepository contractRepository;
	@Mock
	private LegalTerminationCauseRepository legalTerminationCauseRepository;
	@Mock
	private NonRehireReasonRepository nonRehireReasonRepository;
	@Mock
	private TerminationRepository terminationRepository;

	@InjectMocks
	private RequestValidation requestValidation;

	@Test
	void validateCreate_conDatosValidos_noLanzaExcepcion() {
		todasLasFkExisten();

		assertThatCode(() -> requestValidation.validateCreate(dtoValido()))
			.doesNotThrowAnyException();
	}

	@Test
	void validateCreate_conProjectIdInexistente_lanzaErrorEnProjectId() {
		when(projectRepository.existsById(PROJECT_ID)).thenReturn(false);
		lenientFksExisten();

		assertThatThrownBy(() -> requestValidation.validateCreate(dtoValido()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("projectId"));
	}

	@Test
	void validateCreate_conStaffIdInexistente_lanzaErrorEnStaffId() {
		todasLasFkExisten();
		when(staffRepository.existsById(STAFF_ID)).thenReturn(false);

		assertThatThrownBy(() -> requestValidation.validateCreate(dtoValido()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("staffId"));
	}

	@Test
	void validateCreate_conActionDistintaDeCreate_lanzaErrorEnAction() {
		lenient().when(projectRepository.existsById(PROJECT_ID)).thenReturn(true);

		CreateRequestDto dto = dtoValido();
		dto.setAction(RequestAction.EDIT);

		assertThatThrownBy(() -> requestValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("action"));
	}

	@Test
	void validateCreate_sinContractData_lanzaErrorEnContractData() {
		lenient().when(projectRepository.existsById(PROJECT_ID)).thenReturn(true);

		CreateRequestDto dto = dtoValido();
		dto.setContractData(null);

		assertThatThrownBy(() -> requestValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("contractData"));
	}

	@Test
	void validateCreate_conEndDateAntesDeStartDate_lanzaErrorEnEndDate() {
		todasLasFkExisten();

		CreateRequestDto dto = dtoValido();
		dto.getContractData().setStartDate(LocalDate.now().plusDays(10));
		dto.getContractData().setEndDate(LocalDate.now().plusDays(1));

		assertThatThrownBy(() -> requestValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("endDate"));
	}

	@Test
	void validateCreate_conStartDateAnteriorAHoy_lanzaErrorEnStartDate() {
		todasLasFkExisten();

		CreateRequestDto dto = dtoValido();
		dto.getContractData().setStartDate(LocalDate.now().minusDays(1));

		assertThatThrownBy(() -> requestValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("startDate"));
	}

	@Test
	void validateCreate_conTipoPlazoFijoSinEndDate_lanzaErrorEnEndDate() {
		todasLasFkExisten();
		when(contractTypeRepository.findById(CONTRACT_TYPE_ID)).thenReturn(Optional.of(contractTypePlazoFijo()));

		CreateRequestDto dto = dtoValido();
		dto.getContractData().setEndDate(null);

		assertThatThrownBy(() -> requestValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("endDate"));
	}

	@Test
	void validateCreate_conStaffConContratoActivo_lanzaErrorEnStaffId() {
		todasLasFkExisten();
		when(contractRepository.findByStaffIdAndStatus_Code(STAFF_ID, "ACTIVE"))
			.thenReturn(Optional.of(new Contract()));

		assertThatThrownBy(() -> requestValidation.validateCreate(dtoValido()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("staffId"));
	}

	@Test
	void validateCreate_finiquitoConDatosValidos_noLanzaExcepcion() {
		Contract contratoActivo = contratoActivo();
		when(contractRepository.findByStaffIdAndStatus_Code(STAFF_ID, "ACTIVE")).thenReturn(Optional.of(contratoActivo));
		lenient().when(staffRepository.existsById(STAFF_ID)).thenReturn(true);
		lenient().when(legalTerminationCauseRepository.findById(10L)).thenReturn(Optional.of(causaLegalActiva()));
		lenient().when(terminationRepository.findByContract_IdAndStatus_CodeIn(any(), any())).thenReturn(List.of());

		assertThatCode(() -> requestValidation.validateCreate(dtoFiniquitoValido()))
			.doesNotThrowAnyException();
	}

	@Test
	void validateCreate_finiquitoStaffSinContratoActivo_lanzaErrorEnStaffId() {
		when(staffRepository.existsById(STAFF_ID)).thenReturn(true);
		when(contractRepository.findByStaffIdAndStatus_Code(STAFF_ID, "ACTIVE")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> requestValidation.validateCreate(dtoFiniquitoValido()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("staffId"));
	}

	@Test
	void validateCreate_finiquitoCausaLegalInexistente_lanzaErrorEnLegalTerminationCauseId() {
		when(contractRepository.findByStaffIdAndStatus_Code(STAFF_ID, "ACTIVE")).thenReturn(Optional.of(contratoActivo()));
		lenient().when(staffRepository.existsById(STAFF_ID)).thenReturn(true);
		when(legalTerminationCauseRepository.findById(10L)).thenReturn(Optional.empty());
		lenient().when(terminationRepository.findByContract_IdAndStatus_CodeIn(any(), any())).thenReturn(List.of());

		assertThatThrownBy(() -> requestValidation.validateCreate(dtoFiniquitoValido()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("legalTerminationCauseId"));
	}

	@Test
	void validateCreate_finiquitoDefinitivoSinMotivoDeNoRecontratacion_lanzaErrorEnNonRehireReasonId() {
		when(contractRepository.findByStaffIdAndStatus_Code(STAFF_ID, "ACTIVE")).thenReturn(Optional.of(contratoActivo()));
		lenient().when(staffRepository.existsById(STAFF_ID)).thenReturn(true);
		lenient().when(legalTerminationCauseRepository.findById(10L)).thenReturn(Optional.of(causaLegalActiva()));
		lenient().when(terminationRepository.findByContract_IdAndStatus_CodeIn(any(), any())).thenReturn(List.of());

		CreateRequestDto dto = dtoFiniquitoValido();
		dto.getTerminationData().setDefinitiveTermination(true);
		dto.getTerminationData().setNonRehireReasonId(null);

		assertThatThrownBy(() -> requestValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("nonRehireReasonId"));
	}

	@Test
	void validateCreate_finiquitoNoDefinitivoConMotivoDeNoRecontratacion_lanzaErrorEnNonRehireReasonId() {
		when(contractRepository.findByStaffIdAndStatus_Code(STAFF_ID, "ACTIVE")).thenReturn(Optional.of(contratoActivo()));
		lenient().when(staffRepository.existsById(STAFF_ID)).thenReturn(true);
		lenient().when(legalTerminationCauseRepository.findById(10L)).thenReturn(Optional.of(causaLegalActiva()));
		lenient().when(terminationRepository.findByContract_IdAndStatus_CodeIn(any(), any())).thenReturn(List.of());

		CreateRequestDto dto = dtoFiniquitoValido();
		dto.getTerminationData().setDefinitiveTermination(false);
		dto.getTerminationData().setNonRehireReasonId(20L);

		assertThatThrownBy(() -> requestValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("nonRehireReasonId"));
	}

	@Test
	void validateCreate_finiquitoConFechaMuyRetroactiva_lanzaErrorEnTerminationDate() {
		when(contractRepository.findByStaffIdAndStatus_Code(STAFF_ID, "ACTIVE")).thenReturn(Optional.of(contratoActivo()));
		lenient().when(staffRepository.existsById(STAFF_ID)).thenReturn(true);
		lenient().when(legalTerminationCauseRepository.findById(10L)).thenReturn(Optional.of(causaLegalActiva()));
		lenient().when(terminationRepository.findByContract_IdAndStatus_CodeIn(any(), any())).thenReturn(List.of());

		CreateRequestDto dto = dtoFiniquitoValido();
		dto.getTerminationData().setTerminationDate(LocalDate.now().minusDays(30));

		assertThatThrownBy(() -> requestValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("terminationDate"));
	}

	@Test
	void validateCreate_finiquitoConFiniquitoYaPendienteParaElContrato_lanzaErrorEnStaffId() {
		Contract contratoActivo = contratoActivo();
		contratoActivo.setId(99L);
		when(contractRepository.findByStaffIdAndStatus_Code(STAFF_ID, "ACTIVE")).thenReturn(Optional.of(contratoActivo));
		lenient().when(staffRepository.existsById(STAFF_ID)).thenReturn(true);
		lenient().when(legalTerminationCauseRepository.findById(10L)).thenReturn(Optional.of(causaLegalActiva()));
		when(terminationRepository.findByContract_IdAndStatus_CodeIn(99L, List.of("PENDING_APPROVAL", "PENDING_VALIDATION")))
			.thenReturn(List.of(new Termination()));

		assertThatThrownBy(() -> requestValidation.validateCreate(dtoFiniquitoValido()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("staffId"));
	}

	private Contract contratoActivo() {
		Contract contrato = new Contract();
		contrato.setStartDate(LocalDate.now().minusYears(1));
		return contrato;
	}

	private LegalTerminationCause causaLegalActiva() {
		LegalTerminationCause causa = new LegalTerminationCause();
		causa.setActive(true);
		return causa;
	}

	private CreateRequestDto dtoFiniquitoValido() {
		TerminationPendingDataDto terminationData = new TerminationPendingDataDto();
		terminationData.setStaffId(STAFF_ID);
		terminationData.setTerminationDate(LocalDate.now());
		terminationData.setLegalTerminationCauseId(10L);
		terminationData.setDefinitiveTermination(false);

		CreateRequestDto dto = new CreateRequestDto();
		dto.setEntityType(RequestEntityType.TERMINATION);
		dto.setAction(RequestAction.CREATE);
		dto.setTerminationData(terminationData);
		return dto;
	}

	private ContractType contractTypePlazoFijo() {
		ContractType contractType = new ContractType();
		contractType.setCode("F");
		return contractType;
	}

	private void todasLasFkExisten() {
		when(projectRepository.existsById(PROJECT_ID)).thenReturn(true);
		lenientFksExisten();
	}

	private void lenientFksExisten() {
		lenient().when(staffRepository.existsById(STAFF_ID)).thenReturn(true);
		lenient().when(companyRepository.existsById(COMPANY_ID)).thenReturn(true);
		lenient().when(contractTypeRepository.existsById(CONTRACT_TYPE_ID)).thenReturn(true);
		lenient().when(jobTitleRepository.existsById(JOB_TITLE_ID)).thenReturn(true);
		lenient().when(siteRepository.existsById(SITE_ID)).thenReturn(true);
		lenient().when(contractTypeRepository.findById(CONTRACT_TYPE_ID))
			.thenReturn(Optional.of(contractTypeIndefinido()));
		lenient().when(contractRepository.findByStaffIdAndStatus_Code(any(), any())).thenReturn(Optional.empty());
	}

	private ContractType contractTypeIndefinido() {
		ContractType contractType = new ContractType();
		contractType.setCode("I");
		return contractType;
	}

	private CreateRequestDto dtoValido() {
		ContractPendingDataDto contractData = new ContractPendingDataDto();
		contractData.setStaffId(STAFF_ID);
		contractData.setCompanyId(COMPANY_ID);
		contractData.setContractTypeId(CONTRACT_TYPE_ID);
		contractData.setJobTitleId(JOB_TITLE_ID);
		contractData.setSiteId(SITE_ID);
		contractData.setStartDate(LocalDate.now().plusDays(1));
		contractData.setBaseSalary(BigDecimal.valueOf(850000));
		contractData.setWeeklyWorkHours(45);
		contractData.setWorkDays(5);

		CreateRequestDto dto = new CreateRequestDto();
		dto.setEntityType(RequestEntityType.CONTRACT);
		dto.setAction(RequestAction.CREATE);
		dto.setProjectId(PROJECT_ID);
		dto.setContractData(contractData);
		return dto;
	}
}
