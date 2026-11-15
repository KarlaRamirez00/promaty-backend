package com.promaty.rrhh.services.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.rrhh.dto.contract.ContractCountersDto;
import com.promaty.rrhh.dto.contract.ContractDetailDto;
import com.promaty.rrhh.dto.contract.ContractFilterParams;
import com.promaty.rrhh.dto.contract.UpdateContractAttachmentsDto;
import com.promaty.rrhh.dto.contract.UpdateContractDto;
import com.promaty.rrhh.dto.request.ContractPendingDataDto;
import com.promaty.rrhh.services.contract.business.builder.UpdateContractBuilder;
import com.promaty.rrhh.services.contract.business.validation.ContractValidation;
import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.AttachmentEntityType;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.entity.Company;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.ContractType;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.entity.JobTitle;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Site;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.entity.SettingKey;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.services.setting.SettingProvider;
import com.promaty.rrhh.services.shared.RequestEditPolicy;
import com.promaty.rrhh.services.shared.attachment.AttachmentOwner;
import com.promaty.rrhh.services.shared.attachment.AttachmentService;
import com.promaty.rrhh.services.contract.business.ContractAttachmentRules;
import com.promaty.rrhh.services.shared.RequestApprovalActionsResolver;

@ExtendWith(MockitoExtension.class)
class ContractServiceImplTest {

	@Mock
	private ContractRepository contractRepository;
	@Mock
	private RequestRepository requestRepository;
	@Mock
	private SettingProvider settingProvider;
	@Mock
	private AttachmentService attachmentService;
	@Mock
	private RequestEditPolicy requestEditPolicy;
	@Mock
	private ContractValidation contractValidation;
	@Mock
	private UpdateContractBuilder updateContractBuilder;
	@Spy
	private RequestApprovalActionsResolver actionsResolver = new RequestApprovalActionsResolver();

	@InjectMocks
	private ContractServiceImpl service;

	@AfterEach
	void limpiarContextoDeSeguridad() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getContractDetail_editableConPermisoDeActualizar_incluyeAccionUpdate() {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
			"juan", null, List.of(new SimpleGrantedAuthority("contract.update"))
		));
		Request request = new Request();
		request.setStatus(contratoCompleto().getStatus());
		when(contractRepository.findById(1L)).thenReturn(Optional.of(contratoCompleto()));
		when(requestRepository.findByEntityTypeAndEntityId(RequestEntityType.CONTRACT, 1L)).thenReturn(Optional.of(request));
		when(requestEditPolicy.isEditable(request)).thenReturn(true);

		ContractDetailDto detalle = service.getContractDetail(1L);

		assertThat(detalle.getActions()).containsExactly(Action.UPDATE);
		verify(attachmentService).requirements(new AttachmentOwner(AttachmentEntityType.CONTRACT, 1L), ContractAttachmentRules.rules());
	}

	@Test
	void updateContract_validaAplicaYActualizaLaSolicitudConElProyectoNuevo() {
		Contract contrato = contratoCompleto();
		Request request = new Request();
		Project otroProyecto = new Project();
		otroProyecto.setId(2L);
		UpdateContractDto dto = new UpdateContractDto();
		when(contractRepository.findById(1L)).thenReturn(Optional.of(contrato));
		when(requestEditPolicy.requireEditable(RequestEntityType.CONTRACT, 1L)).thenReturn(request);
		doAnswer(invocation -> {
			contrato.setProject(otroProyecto);
			return null;
		}).when(updateContractBuilder).apply(contrato, dto);

		service.updateContract(1L, dto, List.of());

		InOrder orden = inOrder(contractValidation, updateContractBuilder, attachmentService, requestEditPolicy, contractRepository);
		orden.verify(contractValidation).validateUpdate(contrato, dto);
		orden.verify(updateContractBuilder).apply(contrato, dto);
		orden.verify(attachmentService).sync(new AttachmentOwner(AttachmentEntityType.CONTRACT, 1L), ContractAttachmentRules.rules(), null, List.of());
		orden.verify(requestEditPolicy).replacePendingData(eq(request), any(ContractPendingDataDto.class));
		orden.verify(contractRepository).save(contrato);
		assertThat(request.getProject()).isEqualTo(otroProyecto);
	}

	@Test
	void updateContract_conErroresDeValidacion_noAplicaNiGuarda() {
		when(contractRepository.findById(1L)).thenReturn(Optional.of(contratoCompleto()));
		when(requestEditPolicy.requireEditable(RequestEntityType.CONTRACT, 1L)).thenReturn(new Request());
		doThrow(new BusinessValidationException("x", Map.of("projectId", "sin acceso")))
			.when(contractValidation).validateUpdate(any(), any());

		assertThatThrownBy(() -> service.updateContract(1L, new UpdateContractDto(), List.of()))
			.isInstanceOf(BusinessValidationException.class);

		verifyNoInteractions(updateContractBuilder, attachmentService);
		verify(contractRepository, never()).save(any());
	}

	@Test
	void updateAttachments_enPendingApproval_sincronizaSinCambiarElEstado() {
		Contract contrato = contratoCompleto();
		Request request = new Request();
		UpdateContractAttachmentsDto dto = new UpdateContractAttachmentsDto();
		dto.setAttachments(List.of());
		when(contractRepository.findById(1L)).thenReturn(Optional.of(contrato));
		when(requestEditPolicy.requireEditable(RequestEntityType.CONTRACT, 1L)).thenReturn(request);

		service.updateAttachments(1L, dto, List.of());

		verify(attachmentService).sync(new AttachmentOwner(AttachmentEntityType.CONTRACT, 1L), ContractAttachmentRules.rules(), List.of(), List.of());
		verify(contractRepository, never()).save(any());
		assertThat(contrato.getStatus().getCode()).isEqualTo("PENDING_APPROVAL");
	}

	@Test
	void updateAttachments_trasAprobacionDeJefatura_vuelveAPendingApproval() {
		Contract contrato = contratoCompleto();
		Request request = new Request();
		PlatformStatus pendienteAprobacion = new PlatformStatus();
		pendienteAprobacion.setCode("PENDING_APPROVAL");
		when(contractRepository.findById(1L)).thenReturn(Optional.of(contrato));
		when(requestEditPolicy.requireEditable(RequestEntityType.CONTRACT, 1L)).thenReturn(request);
		when(requestEditPolicy.returnToPendingApproval(request)).thenReturn(true);
		when(requestEditPolicy.pendingApprovalStatus("contract")).thenReturn(pendienteAprobacion);

		service.updateAttachments(1L, new UpdateContractAttachmentsDto(), List.of());

		assertThat(contrato.getStatus()).isEqualTo(pendienteAprobacion);
		verify(contractRepository).save(contrato);
	}

	@Test
	void updateAttachments_contratoAprobado_noTocaLosAdjuntos() {
		when(contractRepository.findById(1L)).thenReturn(Optional.of(contratoCompleto()));
		when(requestEditPolicy.requireEditable(RequestEntityType.CONTRACT, 1L))
			.thenThrow(new BusinessValidationException("x", Map.of("status", "no editable")));

		assertThatThrownBy(() -> service.updateAttachments(1L, new UpdateContractAttachmentsDto(), List.of()))
			.isInstanceOf(BusinessValidationException.class);

		verifyNoInteractions(attachmentService);
	}

	@Test
	void getContractDetail_registroNoExiste_lanzaResourceNotFoundException() {
		when(contractRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getContractDetail(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getContractDetail_registroExiste_retornaDetalleMapeadoConSusRelaciones() {
		when(contractRepository.findById(1L)).thenReturn(Optional.of(contratoCompleto()));

		ContractDetailDto detalle = service.getContractDetail(1L);

		assertThat(detalle.getStaff().getIdentificationNumber()).isEqualTo("12345678-5");
		assertThat(detalle.getCompany().getName()).isEqualTo("Constructora Promaty S.A.");
		assertThat(detalle.getCostCenterCode()).isEqualTo("00824");
		assertThat(detalle.getProject().getId()).isEqualTo(1L);
		assertThat(detalle.getStatus().getCode()).isEqualTo("PENDING_APPROVAL");
	}

	@Test
	void getCounters_delegaEnRepositoryConteoPorStatusYRangoDeFechas() {
		when(contractRepository.count(any(Specification.class))).thenReturn(4L, 2L);
		when(settingProvider.getInt(SettingKey.EXPIRING_CONTRACT_DAYS)).thenReturn(15);

		ContractCountersDto contadores = service.getCounters();

		assertThat(contadores.getExpired()).isEqualTo(4L);
		assertThat(contadores.getExpiringSoon()).isEqualTo(2L);
		assertThat(contadores.getExpiringSoonDays()).isEqualTo(15);
	}

	@Test
	void listContracts_conExpiringSoonTrue_leeLosDiasDelSettingYConsultaElRepository() {
		ContractFilterParams filtros = new ContractFilterParams();
		filtros.setExpiringSoon(true);
		when(settingProvider.getInt(SettingKey.EXPIRING_CONTRACT_DAYS)).thenReturn(15);
		when(contractRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

		service.listContracts(filtros, Pageable.unpaged());

		verify(settingProvider).getInt(SettingKey.EXPIRING_CONTRACT_DAYS);
	}

	@Test
	void listContracts_sinExpiringSoon_noConsultaElSetting() {
		when(contractRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

		service.listContracts(new ContractFilterParams(), Pageable.unpaged());

		verifyNoInteractions(settingProvider);
	}

	private Contract contratoCompleto() {
		Staff staff = new Staff();
		staff.setId(1L);
		staff.setIdentificationType(IdentificationType.RUT);
		staff.setIdentificationNumber("12345678-5");
		staff.setFirstName("Juan");
		staff.setPaternalLastName("Perez");
		staff.setMaternalLastName("Soto");

		Company company = new Company();
		company.setId(1L);
		company.setName("Constructora Promaty S.A.");

		ContractType contractType = new ContractType();
		contractType.setId(1L);
		contractType.setName("Contrato indefinido");

		JobTitle jobTitle = new JobTitle();
		jobTitle.setId(1L);
		jobTitle.setName("Albañiles");

		Site site = new Site();
		site.setId(1L);
		site.setName("Casa matriz");

		Project project = new Project();
		project.setId(1L);
		project.setCostCenterCode("00824");

		PlatformStatus status = new PlatformStatus();
		status.setId(1L);
		status.setName("Pendiente de aprobación");
		status.setCode("PENDING_APPROVAL");

		Contract contract = new Contract();
		contract.setId(1L);
		contract.setStaff(staff);
		contract.setCompany(company);
		contract.setContractType(contractType);
		contract.setJobTitle(jobTitle);
		contract.setSite(site);
		contract.setProject(project);
		contract.setStartDate(LocalDate.of(2026, 1, 1));
		contract.setStatus(status);
		return contract;
	}
}
