package com.promaty.rrhh.services.request.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.promaty.rrhh.entity.Company;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.ContractType;
import com.promaty.rrhh.entity.JobTitle;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.Site;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.repository.CompanyRepository;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.ContractTypeRepository;
import com.promaty.rrhh.repository.JobTitleRepository;
import com.promaty.rrhh.repository.MealTypeRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.SiteRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TransportTypeRepository;
import com.promaty.rrhh.services.holidayprovider.BusinessDayCalculator;
import com.promaty.rrhh.services.rexplus.RexPlusClient;
import com.promaty.rrhh.services.rexplus.RexSyncResult;

@ExtendWith(MockitoExtension.class)
class ContractRequestHandlerTest {

	private static final String PENDING_DATA = """
		{"staffId":2,"companyId":3,"contractTypeId":4,"jobTitleId":5,"siteId":6,
		 "startDate":"2026-01-01","baseSalary":850000,"weeklyWorkHours":45,"workDays":5}
		""";

	@Mock
	private ContractRepository contractRepository;
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
	private PlatformStatusRepository platformStatusRepository;
	@Mock
	private RexPlusClient rexPlusClient;
	@Mock
	private BusinessDayCalculator businessDayCalculator;

	private ContractRequestHandler handler;

	@Test
	void apply_conSincronizacionExitosa_creaContractActivoConContractNumberYRetornaSuId() {
		handler = new ContractRequestHandler(
			new ObjectMapper().registerModule(new JavaTimeModule()),
			contractRepository, staffRepository, companyRepository, contractTypeRepository,
			jobTitleRepository, siteRepository, mealTypeRepository, transportTypeRepository,
			platformStatusRepository, rexPlusClient, businessDayCalculator
		);
		when(businessDayCalculator.countBusinessDaysBetween(any(), any())).thenReturn(0L);

		when(staffRepository.findById(2L)).thenReturn(Optional.of(new Staff()));
		when(companyRepository.findById(3L)).thenReturn(Optional.of(new Company()));
		when(contractTypeRepository.findById(4L)).thenReturn(Optional.of(new ContractType()));
		when(jobTitleRepository.findById(5L)).thenReturn(Optional.of(new JobTitle()));
		when(siteRepository.findById(6L)).thenReturn(Optional.of(new Site()));
		PlatformStatus activo = new PlatformStatus();
		activo.setCode("ACTIVE");
		when(platformStatusRepository.findBySubModuleAndCode("contract", "ACTIVE")).thenReturn(Optional.of(activo));
		when(rexPlusClient.syncContract(any(Contract.class))).thenReturn(RexSyncResult.success("REX-123"));

		Project project = new Project();
		project.setId(1L);
		Request request = new Request();
		request.setProject(project);
		request.setPendingData(PENDING_DATA);

		Contract guardado = new Contract();
		guardado.setId(99L);
		when(contractRepository.save(any(Contract.class))).thenReturn(guardado);

		Long id = handler.apply(request);

		assertThat(id).isEqualTo(99L);
	}

	@Test
	void apply_conSincronizacionFallida_dejaContractEnSyncError() {
		handler = new ContractRequestHandler(
			new ObjectMapper().registerModule(new JavaTimeModule()),
			contractRepository, staffRepository, companyRepository, contractTypeRepository,
			jobTitleRepository, siteRepository, mealTypeRepository, transportTypeRepository,
			platformStatusRepository, rexPlusClient, businessDayCalculator
		);
		when(businessDayCalculator.countBusinessDaysBetween(any(), any())).thenReturn(0L);

		when(staffRepository.findById(2L)).thenReturn(Optional.of(new Staff()));
		when(companyRepository.findById(3L)).thenReturn(Optional.of(new Company()));
		when(contractTypeRepository.findById(4L)).thenReturn(Optional.of(new ContractType()));
		when(jobTitleRepository.findById(5L)).thenReturn(Optional.of(new JobTitle()));
		when(siteRepository.findById(6L)).thenReturn(Optional.of(new Site()));
		PlatformStatus syncError = new PlatformStatus();
		syncError.setCode("SYNC_ERROR");
		when(platformStatusRepository.findBySubModuleAndCode("contract", "SYNC_ERROR")).thenReturn(Optional.of(syncError));
		when(rexPlusClient.syncContract(any(Contract.class))).thenReturn(RexSyncResult.failure("Rex+ no respondió."));

		Project project = new Project();
		project.setId(1L);
		Request request = new Request();
		request.setProject(project);
		request.setPendingData(PENDING_DATA);

		Contract guardado = new Contract();
		guardado.setId(100L);
		when(contractRepository.save(any(Contract.class))).thenReturn(guardado);

		Long id = handler.apply(request);

		ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
		verify(contractRepository).save(captor.capture());

		assertThat(id).isEqualTo(100L);
		assertThat(captor.getValue().getStatus()).isEqualTo(syncError);
		assertThat(captor.getValue().getContractNumber()).isNull();
	}

	@Test
	void apply_conPendingDataCorrupto_lanzaIllegalState() {
		handler = new ContractRequestHandler(
			new ObjectMapper(), contractRepository, staffRepository, companyRepository,
			contractTypeRepository, jobTitleRepository, siteRepository, mealTypeRepository,
			transportTypeRepository, platformStatusRepository, rexPlusClient, businessDayCalculator
		);

		Request request = new Request();
		request.setPendingData("{esto-no-es-json");

		assertThatThrownBy(() -> handler.apply(request)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void apply_conPlazoLegalSuperado_dejaContractEnLateRegistrationSinLlamarRexPlus() {
		handler = new ContractRequestHandler(
			new ObjectMapper().registerModule(new JavaTimeModule()),
			contractRepository, staffRepository, companyRepository, contractTypeRepository,
			jobTitleRepository, siteRepository, mealTypeRepository, transportTypeRepository,
			platformStatusRepository, rexPlusClient, businessDayCalculator
		);
		when(businessDayCalculator.countBusinessDaysBetween(any(), any())).thenReturn(20L);

		when(staffRepository.findById(2L)).thenReturn(Optional.of(new Staff()));
		when(companyRepository.findById(3L)).thenReturn(Optional.of(new Company()));
		when(contractTypeRepository.findById(4L)).thenReturn(Optional.of(new ContractType()));
		when(jobTitleRepository.findById(5L)).thenReturn(Optional.of(new JobTitle()));
		when(siteRepository.findById(6L)).thenReturn(Optional.of(new Site()));
		PlatformStatus fueraDePlazo = new PlatformStatus();
		fueraDePlazo.setCode("LATE_REGISTRATION");
		when(platformStatusRepository.findBySubModuleAndCode("contract", "LATE_REGISTRATION"))
			.thenReturn(Optional.of(fueraDePlazo));

		Project project = new Project();
		project.setId(1L);
		Request request = new Request();
		request.setProject(project);
		request.setPendingData(PENDING_DATA);

		Contract guardado = new Contract();
		guardado.setId(101L);
		when(contractRepository.save(any(Contract.class))).thenReturn(guardado);

		Long id = handler.apply(request);

		ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
		verify(contractRepository).save(captor.capture());
		assertThat(id).isEqualTo(101L);
		assertThat(captor.getValue().getStatus()).isEqualTo(fueraDePlazo);
		verify(rexPlusClient, never()).syncContract(any());
	}
}
