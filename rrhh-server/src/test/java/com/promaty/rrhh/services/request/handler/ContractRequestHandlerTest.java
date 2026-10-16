package com.promaty.rrhh.services.request.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.promaty.rrhh.entity.ApprovalDecision;
import com.promaty.rrhh.entity.ApprovalLevel;
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

	@BeforeEach
	void setUp() {
		handler = new ContractRequestHandler(
			new ObjectMapper().registerModule(new JavaTimeModule()),
			contractRepository, staffRepository, companyRepository, contractTypeRepository,
			jobTitleRepository, siteRepository, mealTypeRepository, transportTypeRepository,
			platformStatusRepository, rexPlusClient, businessDayCalculator
		);
	}

	@Test
	void create_conPendingDataValido_creaContractEnPendingApprovalYRetornaSuId() {
		when(staffRepository.findById(2L)).thenReturn(Optional.of(new Staff()));
		when(companyRepository.findById(3L)).thenReturn(Optional.of(new Company()));
		when(contractTypeRepository.findById(4L)).thenReturn(Optional.of(new ContractType()));
		when(jobTitleRepository.findById(5L)).thenReturn(Optional.of(new JobTitle()));
		when(siteRepository.findById(6L)).thenReturn(Optional.of(new Site()));
		PlatformStatus pendienteAprobacion = new PlatformStatus();
		pendienteAprobacion.setCode("PENDING_APPROVAL");
		when(platformStatusRepository.findBySubModuleAndCode("contract", "PENDING_APPROVAL"))
			.thenReturn(Optional.of(pendienteAprobacion));

		Project project = new Project();
		project.setId(1L);
		Request request = new Request();
		request.setProject(project);
		request.setPendingData(PENDING_DATA);

		Contract guardado = new Contract();
		guardado.setId(99L);
		when(contractRepository.save(any(Contract.class))).thenReturn(guardado);

		Long id = handler.create(request);

		assertThat(id).isEqualTo(99L);
		verify(rexPlusClient, never()).syncContract(any());
	}

	@Test
	void create_conPendingDataCorrupto_lanzaIllegalState() {
		Request request = new Request();
		request.setPendingData("{esto-no-es-json");

		assertThatThrownBy(() -> handler.create(request)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void applyDecision_conRechazo_dejaContractEnRejected() {
		Request request = new Request();
		request.setEntityId(50L);
		Contract contract = new Contract();
		PlatformStatus pendienteContrato = estadoStaffConCodigo("PENDING_CONTRACT");
		Staff staff = staffConEstado(pendienteContrato);
		contract.setStaff(staff);
		when(contractRepository.findById(50L)).thenReturn(Optional.of(contract));
		PlatformStatus rechazado = new PlatformStatus();
		rechazado.setCode("REJECTED");
		when(platformStatusRepository.findBySubModuleAndCode("contract", "REJECTED")).thenReturn(Optional.of(rechazado));
		when(platformStatusRepository.findBySubModuleAndCode("staff", "PENDING_CONTRACT")).thenReturn(Optional.of(pendienteContrato));

		handler.applyDecision(request, ApprovalLevel.PROJECT_MANAGER, ApprovalDecision.REJECTED);

		assertThat(contract.getStatus()).isEqualTo(rechazado);
		assertThat(staff.getStatus()).isEqualTo(pendienteContrato);
		verify(contractRepository).save(contract);
		verify(rexPlusClient, never()).syncContract(any());
	}

	@Test
	void applyDecision_conAprobacionNivelJefatura_dejaContractEnPendingValidationSinLlamarRexPlus() {
		Request request = new Request();
		request.setEntityId(51L);
		Contract contract = new Contract();
		when(contractRepository.findById(51L)).thenReturn(Optional.of(contract));
		PlatformStatus pendienteValidacion = new PlatformStatus();
		pendienteValidacion.setCode("PENDING_VALIDATION");
		when(platformStatusRepository.findBySubModuleAndCode("contract", "PENDING_VALIDATION"))
			.thenReturn(Optional.of(pendienteValidacion));

		handler.applyDecision(request, ApprovalLevel.PROJECT_MANAGER, ApprovalDecision.APPROVED);

		assertThat(contract.getStatus()).isEqualTo(pendienteValidacion);
		verify(rexPlusClient, never()).syncContract(any());
	}

	@Test
	void applyDecision_conValidacionRrhhYSincronizacionExitosa_dejaContractActivoConContractNumber() {
		Request request = new Request();
		request.setEntityId(52L);
		Contract contract = new Contract();
		contract.setStartDate(java.time.LocalDate.now());
		contract.setContractType(new ContractType());
		PlatformStatus pendienteContrato = estadoStaffConCodigo("PENDING_CONTRACT");
		Staff staff = staffConEstado(pendienteContrato);
		contract.setStaff(staff);
		when(contractRepository.findById(52L)).thenReturn(Optional.of(contract));
		when(businessDayCalculator.countBusinessDaysBetween(any(), any())).thenReturn(0L);
		when(rexPlusClient.syncContract(contract)).thenReturn(RexSyncResult.success("REX-123"));
		PlatformStatus activo = new PlatformStatus();
		activo.setCode("ACTIVE");
		when(platformStatusRepository.findBySubModuleAndCode("contract", "ACTIVE")).thenReturn(Optional.of(activo));
		PlatformStatus activoStaff = estadoStaffConCodigo("ACTIVE");
		when(platformStatusRepository.findBySubModuleAndCode("staff", "ACTIVE")).thenReturn(Optional.of(activoStaff));

		handler.applyDecision(request, ApprovalLevel.HR, ApprovalDecision.APPROVED);

		assertThat(contract.getStatus()).isEqualTo(activo);
		assertThat(contract.getRexContractNumber()).isEqualTo("REX-123");
		assertThat(staff.getStatus()).isEqualTo(activoStaff);
	}

	private PlatformStatus estadoStaffConCodigo(String code) {
		PlatformStatus estado = new PlatformStatus();
		estado.setCode(code);
		return estado;
	}

	private Staff staffConEstado(PlatformStatus estado) {
		Staff staff = new Staff();
		staff.setStatus(estado);
		return staff;
	}

	@Test
	void applyDecision_conValidacionRrhhYSincronizacionFallida_dejaContractEnSyncError() {
		Request request = new Request();
		request.setEntityId(53L);
		Contract contract = new Contract();
		contract.setStartDate(java.time.LocalDate.now());
		contract.setContractType(new ContractType());
		when(contractRepository.findById(53L)).thenReturn(Optional.of(contract));
		when(businessDayCalculator.countBusinessDaysBetween(any(), any())).thenReturn(0L);
		when(rexPlusClient.syncContract(contract)).thenReturn(RexSyncResult.failure("Rex+ no respondió."));
		PlatformStatus syncError = new PlatformStatus();
		syncError.setCode("SYNC_ERROR");
		when(platformStatusRepository.findBySubModuleAndCode("contract", "SYNC_ERROR")).thenReturn(Optional.of(syncError));

		handler.applyDecision(request, ApprovalLevel.HR, ApprovalDecision.APPROVED);

		ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
		verify(contractRepository).save(captor.capture());
		assertThat(captor.getValue().getStatus()).isEqualTo(syncError);
		assertThat(captor.getValue().getRexContractNumber()).isNull();
	}

	@Test
	void applyDecision_conPlazoLegalSuperado_dejaContractEnLateRegistrationSinLlamarRexPlus() {
		Request request = new Request();
		request.setEntityId(54L);
		Contract contract = new Contract();
		contract.setStartDate(java.time.LocalDate.now().minusDays(30));
		ContractType contractType = new ContractType();
		contractType.setCode("I");
		contract.setContractType(contractType);
		when(contractRepository.findById(54L)).thenReturn(Optional.of(contract));
		when(businessDayCalculator.countBusinessDaysBetween(any(), any())).thenReturn(20L);
		PlatformStatus fueraDePlazo = new PlatformStatus();
		fueraDePlazo.setCode("LATE_REGISTRATION");
		when(platformStatusRepository.findBySubModuleAndCode("contract", "LATE_REGISTRATION"))
			.thenReturn(Optional.of(fueraDePlazo));

		handler.applyDecision(request, ApprovalLevel.HR, ApprovalDecision.APPROVED);

		ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
		verify(contractRepository).save(captor.capture());
		assertThat(captor.getValue().getStatus()).isEqualTo(fueraDePlazo);
		verify(rexPlusClient, never()).syncContract(any());
	}
}
