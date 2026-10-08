package com.promaty.rrhh.services.request.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.request.ContractPendingDataDto;
import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.entity.RequestAction;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.CompanyRepository;
import com.promaty.rrhh.repository.ContractTypeRepository;
import com.promaty.rrhh.repository.JobTitleRepository;
import com.promaty.rrhh.repository.MealTypeRepository;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.repository.SiteRepository;
import com.promaty.rrhh.repository.StaffRepository;
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
		dto.getContractData().setStartDate(LocalDate.of(2026, 1, 10));
		dto.getContractData().setEndDate(LocalDate.of(2026, 1, 1));

		assertThatThrownBy(() -> requestValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("endDate"));
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
	}

	private CreateRequestDto dtoValido() {
		ContractPendingDataDto contractData = new ContractPendingDataDto();
		contractData.setStaffId(STAFF_ID);
		contractData.setCompanyId(COMPANY_ID);
		contractData.setContractTypeId(CONTRACT_TYPE_ID);
		contractData.setJobTitleId(JOB_TITLE_ID);
		contractData.setSiteId(SITE_ID);
		contractData.setStartDate(LocalDate.of(2026, 1, 1));
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
