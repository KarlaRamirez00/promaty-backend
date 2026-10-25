package com.promaty.rrhh.services.termination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.dto.termination.AnnulTerminationDto;
import com.promaty.rrhh.dto.termination.TerminationDetailDto;
import com.promaty.rrhh.dto.termination.UpdateTerminationDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.entity.JobTitle;
import com.promaty.rrhh.entity.LegalTerminationCause;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.LegalTerminationCauseRepository;
import com.promaty.rrhh.repository.NonRehireReasonRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.services.rexplus.RexPlusClient;
import com.promaty.rrhh.services.rexplus.RexSyncResult;
import com.promaty.rrhh.services.shared.RequestApprovalActionsResolver;
import com.promaty.rrhh.services.termination.business.validation.TerminationAnnulmentValidation;
import com.promaty.rrhh.services.termination.business.validation.TerminationValidation;

@ExtendWith(MockitoExtension.class)
class TerminationServiceImplTest {

	@Mock
	private TerminationRepository terminationRepository;
	@Mock
	private RequestRepository requestRepository;
	@Mock
	private ContractRepository contractRepository;
	@Mock
	private StaffRepository staffRepository;
	@Mock
	private PlatformStatusRepository platformStatusRepository;
	@Mock
	private LegalTerminationCauseRepository legalTerminationCauseRepository;
	@Mock
	private NonRehireReasonRepository nonRehireReasonRepository;
	@Mock
	private TerminationValidation terminationValidation;
	@Mock
	private TerminationAnnulmentValidation terminationAnnulmentValidation;
	@Mock
	private RexPlusClient rexPlusClient;
	@Spy
	private RequestApprovalActionsResolver actionsResolver = new RequestApprovalActionsResolver();

	@InjectMocks
	private TerminationServiceImpl service;

	@AfterEach
	void limpiarContextoDeSeguridad() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getTerminationDetail_registroNoExiste_lanzaResourceNotFoundException() {
		when(terminationRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getTerminationDetail(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getTerminationDetail_registroExiste_retornaDetalleMapeadoConSusRelaciones() {
		when(terminationRepository.findById(1L)).thenReturn(Optional.of(terminacionCompleta()));
		when(requestRepository.findByEntityTypeAndEntityId(RequestEntityType.TERMINATION, 1L)).thenReturn(Optional.empty());

		TerminationDetailDto detalle = service.getTerminationDetail(1L);

		assertThat(detalle.getStaff().getIdentificationNumber()).isEqualTo("12345678-5");
		assertThat(detalle.getCostCenterCode()).isEqualTo("00824");
		assertThat(detalle.getStatus().getCode()).isEqualTo("PENDING_APPROVAL");
	}

	@Test
	void getTerminationDetail_conRequestEnPendingApprovalYPermisoDeAprobar_incluyeAccionApprove() {
		autenticarCon("termination.approve");
		when(terminationRepository.findById(1L)).thenReturn(Optional.of(terminacionCompleta()));
		Request request = new Request();
		request.setId(5L);
		PlatformStatus status = new PlatformStatus();
		status.setCode("PENDING_APPROVAL");
		request.setStatus(status);
		when(requestRepository.findByEntityTypeAndEntityId(RequestEntityType.TERMINATION, 1L)).thenReturn(Optional.of(request));

		TerminationDetailDto detalle = service.getTerminationDetail(1L);

		assertThat(detalle.getRequestId()).isEqualTo(5L);
		assertThat(detalle.getActions()).contains(Action.APPROVE);
	}

	@Test
	void getTerminationDetail_conRequestPendienteYPermisoDeActualizar_incluyeAccionUpdate() {
		autenticarCon("termination.update");
		when(terminationRepository.findById(1L)).thenReturn(Optional.of(terminacionCompleta()));
		Request request = new Request();
		PlatformStatus status = new PlatformStatus();
		status.setCode("PENDING_VALIDATION");
		request.setStatus(status);
		when(requestRepository.findByEntityTypeAndEntityId(RequestEntityType.TERMINATION, 1L)).thenReturn(Optional.of(request));

		TerminationDetailDto detalle = service.getTerminationDetail(1L);

		assertThat(detalle.getActions()).contains(Action.UPDATE);
	}

	@Test
	void updateTermination_registroNoExiste_lanzaResourceNotFoundException() {
		when(terminationRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateTermination(1L, dtoValido()))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void updateTermination_conDatosValidos_validaYGuarda() {
		Termination existente = terminacionCompleta();
		when(terminationRepository.findById(1L)).thenReturn(Optional.of(existente));
		LegalTerminationCause nuevaCausa = new LegalTerminationCause();
		when(legalTerminationCauseRepository.findById(11L)).thenReturn(Optional.of(nuevaCausa));

		UpdateTerminationDto dto = dtoValido();
		dto.setLegalTerminationCauseId(11L);
		service.updateTermination(1L, dto);

		verify(terminationValidation).validateUpdate(existente, dto);
		verify(terminationRepository).save(existente);
		assertThat(existente.getLegalTerminationCause()).isEqualTo(nuevaCausa);
	}

	@Test
	void annulTermination_sincronizacionExitosa_revierteContratoStaffYMarcaAnulado() {
		Termination termination = terminacionAprobada();
		Contract contrato = termination.getContract();
		Staff staff = termination.getStaff();
		Request request = new Request();
		PlatformStatus statusRequestApproved = new PlatformStatus();
		statusRequestApproved.setCode("APPROVED");
		request.setStatus(statusRequestApproved);

		when(terminationRepository.findById(1L)).thenReturn(Optional.of(termination));
		when(rexPlusClient.annulTermination(termination)).thenReturn(RexSyncResult.success("REX-TERM-1"));
		when(platformStatusRepository.findBySubModuleAndCode("contract", "ACTIVE")).thenReturn(Optional.of(statusCon("contract", "ACTIVE")));
		when(platformStatusRepository.findBySubModuleAndCode("staff", "ACTIVE")).thenReturn(Optional.of(statusCon("staff", "ACTIVE")));
		when(platformStatusRepository.findBySubModuleAndCode("termination", "ANNULLED")).thenReturn(Optional.of(statusCon("termination", "ANNULLED")));
		when(platformStatusRepository.findBySubModuleAndCode("request", "ANNULLED")).thenReturn(Optional.of(statusCon("request", "ANNULLED")));
		when(requestRepository.findByEntityTypeAndEntityId(RequestEntityType.TERMINATION, 1L)).thenReturn(Optional.of(request));

		AnnulTerminationDto dto = new AnnulTerminationDto();
		dto.setReason("Error administrativo al aprobar.");
		service.annulTermination(1L, dto);

		assertThat(contrato.getEndDate()).isNull();
		assertThat(contrato.getStatus().getCode()).isEqualTo("ACTIVE");
		assertThat(staff.getStatus().getCode()).isEqualTo("ACTIVE");
		assertThat(staff.getBlockReason()).isNull();
		assertThat(termination.getStatus().getCode()).isEqualTo("ANNULLED");
		assertThat(termination.getAnnulmentReason()).isEqualTo("Error administrativo al aprobar.");
		assertThat(request.getStatus().getCode()).isEqualTo("ANNULLED");
		verify(terminationRepository).save(termination);
		verify(requestRepository).save(request);
	}

	@Test
	void annulTermination_fallaSincronizacionConRex_marcaSyncErrorYLanzaExcepcion() {
		Termination termination = terminacionAprobada();
		when(terminationRepository.findById(1L)).thenReturn(Optional.of(termination));
		when(rexPlusClient.annulTermination(termination)).thenReturn(RexSyncResult.failure("Rex+ no respondió."));
		when(platformStatusRepository.findBySubModuleAndCode("termination", "SYNC_ERROR"))
			.thenReturn(Optional.of(statusCon("termination", "SYNC_ERROR")));

		AnnulTerminationDto dto = new AnnulTerminationDto();
		dto.setReason("Error administrativo al aprobar.");

		assertThatThrownBy(() -> service.annulTermination(1L, dto))
			.isInstanceOf(BusinessValidationException.class);

		assertThat(termination.getStatus().getCode()).isEqualTo("SYNC_ERROR");
		verify(contractRepository, org.mockito.Mockito.never()).save(any());
		verify(staffRepository, org.mockito.Mockito.never()).save(any());
	}

	private PlatformStatus statusCon(String subModule, String code) {
		PlatformStatus status = new PlatformStatus();
		status.setSubModule(subModule);
		status.setCode(code);
		return status;
	}

	private Termination terminacionAprobada() {
		Termination termination = terminacionCompleta();
		PlatformStatus statusAprobado = new PlatformStatus();
		statusAprobado.setCode("APPROVED");
		termination.setStatus(statusAprobado);
		termination.setUpdatedAt(java.time.LocalDateTime.now());

		Staff staff = termination.getStaff();
		PlatformStatus statusStaffBloqueado = new PlatformStatus();
		statusStaffBloqueado.setCode("BLOCKED");
		staff.setStatus(statusStaffBloqueado);
		staff.setBlockReason("Finiquito aprobado.");

		Contract contrato = termination.getContract();
		PlatformStatus statusContratoTerminado = new PlatformStatus();
		statusContratoTerminado.setCode("TERMINATED");
		contrato.setStatus(statusContratoTerminado);
		contrato.setEndDate(termination.getTerminationDate());
		return termination;
	}

	private void autenticarCon(String autoridad) {
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken("usuario", null, List.of(new SimpleGrantedAuthority(autoridad))));
	}

	private UpdateTerminationDto dtoValido() {
		UpdateTerminationDto dto = new UpdateTerminationDto();
		dto.setTerminationDate(LocalDate.now());
		dto.setLegalTerminationCauseId(10L);
		dto.setDefinitiveTermination(false);
		return dto;
	}

	private Termination terminacionCompleta() {
		Staff staff = new Staff();
		staff.setId(1L);
		staff.setIdentificationType(IdentificationType.RUT);
		staff.setIdentificationNumber("12345678-5");
		staff.setFirstName("Juan");
		staff.setPaternalLastName("Perez");
		staff.setMaternalLastName("Soto");

		Project project = new Project();
		project.setCostCenterCode("00824");

		LegalTerminationCause causa = new LegalTerminationCause();
		causa.setId(1L);
		causa.setName("Renuncia voluntaria");

		JobTitle jobTitle = new JobTitle();
		jobTitle.setName("Albañil");

		Contract contract = new Contract();
		contract.setStartDate(LocalDate.of(2025, 1, 1));
		contract.setJobTitle(jobTitle);

		PlatformStatus status = new PlatformStatus();
		status.setId(1L);
		status.setName("Pendiente de aprobación");
		status.setCode("PENDING_APPROVAL");

		Termination termination = new Termination();
		termination.setId(1L);
		termination.setStaff(staff);
		termination.setContract(contract);
		termination.setProject(project);
		termination.setTerminationDate(LocalDate.of(2026, 1, 1));
		termination.setLegalTerminationCause(causa);
		termination.setDefinitiveTermination(false);
		termination.setStatus(status);
		return termination;
	}
}
