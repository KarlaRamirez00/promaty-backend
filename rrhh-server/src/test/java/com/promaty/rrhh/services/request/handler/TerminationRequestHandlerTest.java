package com.promaty.rrhh.services.request.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.LegalTerminationCause;
import com.promaty.rrhh.entity.NonRehireReason;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.LegalTerminationCauseRepository;
import com.promaty.rrhh.repository.NonRehireReasonRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.services.rexplus.RexPlusClient;
import com.promaty.rrhh.services.rexplus.RexSyncResult;

@ExtendWith(MockitoExtension.class)
class TerminationRequestHandlerTest {

	private static final String PENDING_DATA = """
		{"staffId":2,"terminationDate":"2026-01-15","legalTerminationCauseId":3,"definitiveTermination":false}
		""";

	@Mock
	private StaffRepository staffRepository;
	@Mock
	private ContractRepository contractRepository;
	@Mock
	private LegalTerminationCauseRepository legalTerminationCauseRepository;
	@Mock
	private NonRehireReasonRepository nonRehireReasonRepository;
	@Mock
	private TerminationRepository terminationRepository;
	@Mock
	private PlatformStatusRepository platformStatusRepository;
	@Mock
	private RexPlusClient rexPlusClient;

	private TerminationRequestHandler handler;

	@BeforeEach
	void setUp() {
		handler = new TerminationRequestHandler(
			new ObjectMapper().registerModule(new JavaTimeModule()),
			staffRepository, contractRepository, legalTerminationCauseRepository,
			nonRehireReasonRepository, terminationRepository, platformStatusRepository, rexPlusClient
		);
	}

	@Test
	void create_conPendingDataValido_creaTerminationEnPendingApprovalYRetornaSuId() {
		when(staffRepository.findById(2L)).thenReturn(Optional.of(new Staff()));
		when(contractRepository.findByStaffIdAndStatus_Code(2L, "ACTIVE")).thenReturn(Optional.of(new Contract()));
		when(legalTerminationCauseRepository.findById(3L)).thenReturn(Optional.of(new LegalTerminationCause()));
		PlatformStatus pendienteAprobacion = estadoConCodigo("PENDING_APPROVAL");
		when(platformStatusRepository.findBySubModuleAndCode("termination", "PENDING_APPROVAL"))
			.thenReturn(Optional.of(pendienteAprobacion));

		Project project = new Project();
		project.setId(1L);
		Request request = new Request();
		request.setProject(project);
		request.setPendingData(PENDING_DATA);

		Termination guardado = new Termination();
		guardado.setId(99L);
		when(terminationRepository.save(any(Termination.class))).thenReturn(guardado);

		Long id = handler.create(request);

		assertThat(id).isEqualTo(99L);
		verify(rexPlusClient, never()).syncTermination(any());
	}

	@Test
	void create_conPendingDataCorrupto_lanzaIllegalState() {
		Request request = new Request();
		request.setPendingData("{esto-no-es-json");

		assertThatThrownBy(() -> handler.create(request)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void applyDecision_conRechazo_dejaTerminationEnRejectedSinTocarContratoNiStaff() {
		Request request = new Request();
		request.setEntityId(50L);
		Termination termination = terminationConEstado(estadoConCodigo("PENDING_APPROVAL"));
		Contract contract = new Contract();
		termination.setContract(contract);
		when(terminationRepository.findById(50L)).thenReturn(Optional.of(termination));
		PlatformStatus rechazado = estadoConCodigo("REJECTED");
		when(platformStatusRepository.findBySubModuleAndCode("termination", "REJECTED")).thenReturn(Optional.of(rechazado));

		handler.applyDecision(request, ApprovalLevel.PROJECT_MANAGER, ApprovalDecision.REJECTED);

		assertThat(termination.getStatus()).isEqualTo(rechazado);
		assertThat(contract.getStatus()).isNull();
		verify(terminationRepository).save(termination);
		verify(rexPlusClient, never()).syncTermination(any());
	}

	@Test
	void applyDecision_conAprobacionNivelJefatura_dejaTerminationEnPendingValidationSinLlamarRexPlus() {
		Request request = new Request();
		request.setEntityId(51L);
		Termination termination = terminationConEstado(estadoConCodigo("PENDING_APPROVAL"));
		when(terminationRepository.findById(51L)).thenReturn(Optional.of(termination));
		PlatformStatus pendienteValidacion = estadoConCodigo("PENDING_VALIDATION");
		when(platformStatusRepository.findBySubModuleAndCode("termination", "PENDING_VALIDATION"))
			.thenReturn(Optional.of(pendienteValidacion));

		handler.applyDecision(request, ApprovalLevel.PROJECT_MANAGER, ApprovalDecision.APPROVED);

		assertThat(termination.getStatus()).isEqualTo(pendienteValidacion);
		verify(rexPlusClient, never()).syncTermination(any());
	}

	@Test
	void applyDecision_conValidacionRrhhYSincronizacionExitosa_aplicaEfectosSobreContractYStaff() {
		Request request = new Request();
		request.setEntityId(52L);
		Termination termination = terminationConEstado(estadoConCodigo("PENDING_VALIDATION"));
		termination.setTerminationDate(LocalDate.of(2026, 3, 1));
		termination.setDefinitiveTermination(true);
		Contract contract = new Contract();
		contract.setEndDate(LocalDate.of(2027, 11, 20));
		termination.setContract(contract);
		Staff staff = new Staff();
		termination.setStaff(staff);
		when(terminationRepository.findById(52L)).thenReturn(Optional.of(termination));
		when(rexPlusClient.syncTermination(termination)).thenReturn(RexSyncResult.success("REX-TERM-123"));
		PlatformStatus contratoTerminado = estadoConCodigo("TERMINATED");
		when(platformStatusRepository.findBySubModuleAndCode("contract", "TERMINATED")).thenReturn(Optional.of(contratoTerminado));
		PlatformStatus staffBloqueado = estadoConCodigo("BLOCKED");
		when(platformStatusRepository.findBySubModuleAndCode("staff", "BLOCKED")).thenReturn(Optional.of(staffBloqueado));
		PlatformStatus aprobado = estadoConCodigo("APPROVED");
		when(platformStatusRepository.findBySubModuleAndCode("termination", "APPROVED")).thenReturn(Optional.of(aprobado));

		handler.applyDecision(request, ApprovalLevel.HR, ApprovalDecision.APPROVED);

		assertThat(contract.getStatus()).isEqualTo(contratoTerminado);
		assertThat(contract.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 1));
		assertThat(termination.getContractEndDateBeforeTermination()).isEqualTo(LocalDate.of(2027, 11, 20));
		assertThat(staff.getStatus()).isEqualTo(staffBloqueado);
		assertThat(termination.getStatus()).isEqualTo(aprobado);
		assertThat(termination.getRexTerminationNumber()).isEqualTo("REX-TERM-123");
	}

	@Test
	void applyDecision_conDefinitiveTerminationFalso_dejaStaffUnlinked() {
		Request request = new Request();
		request.setEntityId(53L);
		Termination termination = terminationConEstado(estadoConCodigo("PENDING_VALIDATION"));
		termination.setTerminationDate(LocalDate.of(2026, 3, 1));
		termination.setDefinitiveTermination(false);
		Contract contract = new Contract();
		termination.setContract(contract);
		Staff staff = new Staff();
		termination.setStaff(staff);
		when(terminationRepository.findById(53L)).thenReturn(Optional.of(termination));
		when(rexPlusClient.syncTermination(termination)).thenReturn(RexSyncResult.success("REX-TERM-124"));
		when(platformStatusRepository.findBySubModuleAndCode("contract", "TERMINATED")).thenReturn(Optional.of(estadoConCodigo("TERMINATED")));
		PlatformStatus staffDesvinculado = estadoConCodigo("UNLINKED");
		when(platformStatusRepository.findBySubModuleAndCode("staff", "UNLINKED")).thenReturn(Optional.of(staffDesvinculado));
		when(platformStatusRepository.findBySubModuleAndCode("termination", "APPROVED")).thenReturn(Optional.of(estadoConCodigo("APPROVED")));

		handler.applyDecision(request, ApprovalLevel.HR, ApprovalDecision.APPROVED);

		assertThat(staff.getStatus()).isEqualTo(staffDesvinculado);
	}

	@Test
	void applyDecision_conSincronizacionFallida_dejaTerminationEnSyncErrorSinTocarContratoNiStaff() {
		Request request = new Request();
		request.setEntityId(54L);
		Termination termination = terminationConEstado(estadoConCodigo("PENDING_VALIDATION"));
		Contract contract = new Contract();
		termination.setContract(contract);
		Staff staff = new Staff();
		termination.setStaff(staff);
		when(terminationRepository.findById(54L)).thenReturn(Optional.of(termination));
		when(rexPlusClient.syncTermination(termination)).thenReturn(RexSyncResult.failure("Rex+ no respondió."));
		PlatformStatus syncError = estadoConCodigo("SYNC_ERROR");
		when(platformStatusRepository.findBySubModuleAndCode("termination", "SYNC_ERROR")).thenReturn(Optional.of(syncError));

		handler.applyDecision(request, ApprovalLevel.HR, ApprovalDecision.APPROVED);

		ArgumentCaptor<Termination> captor = ArgumentCaptor.forClass(Termination.class);
		verify(terminationRepository).save(captor.capture());
		assertThat(captor.getValue().getStatus()).isEqualTo(syncError);
		assertThat(contract.getStatus()).isNull();
		assertThat(staff.getStatus()).isNull();
	}

	@Test
	void applyDecision_siYaEstabaAprobado_esIdempotenteYNoReaplicaEfectos() {
		Request request = new Request();
		request.setEntityId(55L);
		Termination termination = terminationConEstado(estadoConCodigo("APPROVED"));
		when(terminationRepository.findById(55L)).thenReturn(Optional.of(termination));

		handler.applyDecision(request, ApprovalLevel.HR, ApprovalDecision.APPROVED);

		verify(rexPlusClient, never()).syncTermination(any());
		verify(terminationRepository, never()).save(any());
	}

	@Test
	void create_conNonRehireReasonId_loResuelveYAsigna() {
		when(staffRepository.findById(2L)).thenReturn(Optional.of(new Staff()));
		when(contractRepository.findByStaffIdAndStatus_Code(2L, "ACTIVE")).thenReturn(Optional.of(new Contract()));
		when(legalTerminationCauseRepository.findById(3L)).thenReturn(Optional.of(new LegalTerminationCause()));
		NonRehireReason motivo = new NonRehireReason();
		when(nonRehireReasonRepository.findById(7L)).thenReturn(Optional.of(motivo));
		when(platformStatusRepository.findBySubModuleAndCode("termination", "PENDING_APPROVAL"))
			.thenReturn(Optional.of(estadoConCodigo("PENDING_APPROVAL")));

		Request request = new Request();
		request.setProject(new Project());
		request.setPendingData("""
			{"staffId":2,"terminationDate":"2026-01-15","legalTerminationCauseId":3,
			 "definitiveTermination":true,"nonRehireReasonId":7}
			""");
		ArgumentCaptor<Termination> captor = ArgumentCaptor.forClass(Termination.class);
		when(terminationRepository.save(captor.capture())).thenAnswer(invocation -> captor.getValue());

		handler.create(request);

		assertThat(captor.getValue().getNonRehireReason()).isEqualTo(motivo);
	}

	private PlatformStatus estadoConCodigo(String code) {
		PlatformStatus estado = new PlatformStatus();
		estado.setCode(code);
		return estado;
	}

	private Termination terminationConEstado(PlatformStatus estado) {
		Termination termination = new Termination();
		termination.setStatus(estado);
		return termination;
	}
}
