package com.promaty.rrhh.services.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.rrhh.dto.request.ContractPendingDataResolvedDto;
import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.request.DecideRequestDto;
import com.promaty.rrhh.dto.request.RequestCountersDto;
import com.promaty.rrhh.dto.request.RequestDetailDto;
import com.promaty.rrhh.dto.request.RequestFilterParams;
import com.promaty.rrhh.dto.request.RequestListDto;
import com.promaty.rrhh.entity.Approval;
import com.promaty.rrhh.entity.ApprovalDecision;
import com.promaty.rrhh.entity.ApprovalLevel;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestAction;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ApprovalRepository;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.RequestRejectionReasonRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.repository.projection.RequestStaffDataProjection;
import com.promaty.rrhh.services.request.business.builder.CreateRequestBuilder;
import com.promaty.rrhh.services.request.business.builder.RequestRelationsResolver;
import com.promaty.rrhh.services.request.business.mapper.ContractPendingDataResolver;
import com.promaty.rrhh.services.request.business.validation.RequestDecisionValidation;
import com.promaty.rrhh.services.request.business.validation.RequestValidation;
import com.promaty.rrhh.services.request.handler.RequestHandler;
import com.promaty.rrhh.services.request.handler.RequestHandlerRegistry;
import com.promaty.rrhh.services.shared.RequestApprovalActionsResolver;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

	@Mock
	private RequestRepository requestRepository;
	@Mock
	private ApprovalRepository approvalRepository;
	@Mock
	private RequestRejectionReasonRepository requestRejectionReasonRepository;
	@Mock
	private ContractRepository contractRepository;
	@Mock
	private RequestValidation requestValidation;
	@Mock
	private RequestDecisionValidation requestDecisionValidation;
	@Mock
	private CreateRequestBuilder createRequestBuilder;
	@Mock
	private RequestRelationsResolver relationsResolver;
	@Mock
	private RequestHandlerRegistry handlerRegistry;
	@Mock
	private RequestHandler contractRequestHandler;
	@Mock
	private ContractPendingDataResolver contractPendingDataResolver;
	@Spy
	private RequestApprovalActionsResolver actionsResolver = new RequestApprovalActionsResolver();

	@AfterEach
	void limpiarContextoDeSeguridad() {
		SecurityContextHolder.clearContext();
	}

	@InjectMocks
	private RequestServiceImpl requestService;

	@Test
	void createRequest_validaArmaCreaLaEntidadYGuarda_retornaIdGenerado() {
		CreateRequestDto dto = new CreateRequestDto();
		dto.setEntityType(RequestEntityType.CONTRACT);
		Request request = new Request();
		request.setEntityType(RequestEntityType.CONTRACT);
		Request guardado = new Request();
		guardado.setId(10L);

		when(createRequestBuilder.build(dto)).thenReturn(request);
		when(handlerRegistry.get(RequestEntityType.CONTRACT)).thenReturn(contractRequestHandler);
		when(contractRequestHandler.create(request)).thenReturn(77L);
		when(requestRepository.save(request)).thenReturn(guardado);

		Long id = requestService.createRequest(dto);

		assertThat(id).isEqualTo(10L);
		assertThat(request.getEntityId()).isEqualTo(77L);
		verify(requestValidation).validateCreate(dto);
	}

	@Test
	void decideRequest_aprobarEnPendingApproval_avanzaAPendingValidationYAplicaDecisionEnHandler() {
		Request request = requestConId(1L, RequestEntityType.CONTRACT);
		request.setEntityId(200L);
		DecideRequestDto dto = decisionAprobada();
		when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
		when(requestDecisionValidation.validateDecision(request, dto)).thenReturn(ApprovalLevel.PROJECT_MANAGER);
		PlatformStatus pendingValidation = statusConCodigo("PENDING_VALIDATION");
		when(relationsResolver.resolveNextStatus(ApprovalLevel.PROJECT_MANAGER, ApprovalDecision.APPROVED))
			.thenReturn(pendingValidation);
		when(handlerRegistry.get(RequestEntityType.CONTRACT)).thenReturn(contractRequestHandler);

		requestService.decideRequest(1L, dto);

		assertThat(request.getStatus()).isEqualTo(pendingValidation);
		verify(contractRequestHandler).applyDecision(request, ApprovalLevel.PROJECT_MANAGER, ApprovalDecision.APPROVED);
		verify(approvalRepository).save(any());
		verify(requestRepository).save(request);
	}

	@Test
	void decideRequest_aprobarEnPendingValidation_invocaHandlerConNivelHr() {
		Request request = requestConId(2L, RequestEntityType.CONTRACT);
		request.setEntityId(201L);
		DecideRequestDto dto = decisionAprobada();
		when(requestRepository.findById(2L)).thenReturn(Optional.of(request));
		when(requestDecisionValidation.validateDecision(request, dto)).thenReturn(ApprovalLevel.HR);
		PlatformStatus approved = statusConCodigo("APPROVED");
		when(relationsResolver.resolveNextStatus(ApprovalLevel.HR, ApprovalDecision.APPROVED)).thenReturn(approved);
		when(handlerRegistry.get(RequestEntityType.CONTRACT)).thenReturn(contractRequestHandler);

		requestService.decideRequest(2L, dto);

		assertThat(request.getStatus()).isEqualTo(approved);
		verify(contractRequestHandler).applyDecision(request, ApprovalLevel.HR, ApprovalDecision.APPROVED);
	}

	@Test
	void decideRequest_rechazar_invocaHandlerConDecisionRejected() {
		Request request = requestConId(3L, RequestEntityType.CONTRACT);
		request.setEntityId(202L);
		DecideRequestDto dto = new DecideRequestDto();
		dto.setDecision(ApprovalDecision.REJECTED);
		dto.setRejectionReasonId(1L);
		when(requestRepository.findById(3L)).thenReturn(Optional.of(request));
		when(requestDecisionValidation.validateDecision(request, dto)).thenReturn(ApprovalLevel.PROJECT_MANAGER);
		PlatformStatus rejected = statusConCodigo("REJECTED");
		when(relationsResolver.resolveNextStatus(ApprovalLevel.PROJECT_MANAGER, ApprovalDecision.REJECTED))
			.thenReturn(rejected);
		when(handlerRegistry.get(RequestEntityType.CONTRACT)).thenReturn(contractRequestHandler);

		requestService.decideRequest(3L, dto);

		assertThat(request.getStatus()).isEqualTo(rejected);
		verify(contractRequestHandler).applyDecision(request, ApprovalLevel.PROJECT_MANAGER, ApprovalDecision.REJECTED);
	}

	@Test
	void listRequests_retornaPaginaMapeada() {
		Request request = requestConId(4L, RequestEntityType.CONTRACT);
		request.setProject(proyectoConNombre("Edificio Centro"));
		request.setStatus(statusConCodigo("PENDING_APPROVAL"));
		RequestFilterParams filtros = new RequestFilterParams();
		Pageable pageable = PageRequest.of(0, 20);
		when(requestRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(request)));

		List<RequestListDto> resultado = requestService.listRequests(filtros, pageable).getContent();

		assertThat(resultado).hasSize(1);
		assertThat(resultado.get(0).getId()).isEqualTo(4L);
		assertThat(resultado.get(0).getProjectName()).isEqualTo("Edificio Centro");
	}

	@Test
	void listRequests_conPermisoDeAprobarYStatusPendingApproval_actionsQuedaVacio() {
		autenticarCon("contract.approve");
		Request request = requestConId(6L, RequestEntityType.CONTRACT);
		request.setProject(proyectoConNombre("Edificio Centro"));
		request.setStatus(statusConCodigo("PENDING_APPROVAL"));
		Pageable pageable = PageRequest.of(0, 20);
		when(requestRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(request)));

		List<RequestListDto> resultado = requestService.listRequests(new RequestFilterParams(), pageable).getContent();

		assertThat(resultado.get(0).getActions()).isEmpty();
	}

	@Test
	void listRequests_conSolicitudDeContrato_resuelveDatosDeStaffEnLote() {
		Request request = requestConId(4L, RequestEntityType.CONTRACT);
		request.setEntityId(50L);
		request.setProject(proyectoConNombre("Edificio Centro"));
		request.setStatus(statusConCodigo("PENDING_APPROVAL"));
		Pageable pageable = PageRequest.of(0, 20);
		when(requestRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(request)));
		when(contractRepository.findStaffDataByContractIds(List.of(50L)))
			.thenReturn(List.of(staffDataProjection(50L, "Juan", "Perez", "Soto", "12345678-5", "Albañil")));

		RequestListDto dto = requestService.listRequests(new RequestFilterParams(), pageable).getContent().get(0);

		assertThat(dto.getStaffFirstName()).isEqualTo("Juan");
		assertThat(dto.getStaffPaternalLastName()).isEqualTo("Perez");
		assertThat(dto.getStaffMaternalLastName()).isEqualTo("Soto");
		assertThat(dto.getStaffIdentificationNumber()).isEqualTo("12345678-5");
		assertThat(dto.getStaffPosition()).isEqualTo("Albañil");
	}

	@Test
	void getCounters_delegaEnRepositoryConteoPorStatus() {
		when(requestRepository.count(any(Specification.class))).thenReturn(3L, 5L);

		RequestCountersDto contadores = requestService.getCounters(RequestEntityType.CONTRACT);

		assertThat(contadores.getPendingApproval()).isEqualTo(3L);
		assertThat(contadores.getPendingValidation()).isEqualTo(5L);
	}

	@Test
	void getRequestDetail_conIdExistente_retornaDetalleConHistorialDeApprovals() {
		Request request = requestConId(5L, RequestEntityType.CONTRACT);
		request.setProject(proyectoConNombre("Edificio Centro"));
		request.setStatus(statusConCodigo("APPROVED"));
		when(requestRepository.findById(5L)).thenReturn(Optional.of(request));
		when(handlerRegistry.get(RequestEntityType.CONTRACT)).thenReturn(contractRequestHandler);
		Approval approval = new Approval();
		approval.setLevel(ApprovalLevel.PROJECT_MANAGER);
		approval.setDecision(ApprovalDecision.APPROVED);
		when(approvalRepository.findByRequest_IdOrderByCreatedAtAsc(5L)).thenReturn(List.of(approval));

		RequestDetailDto detalle = requestService.getRequestDetail(5L);

		assertThat(detalle.getId()).isEqualTo(5L);
		assertThat(detalle.getApprovals()).hasSize(1);
		assertThat(detalle.getApprovals().get(0).getLevel()).isEqualTo(ApprovalLevel.PROJECT_MANAGER);
	}

	@Test
	void getRequestDetail_entityTypeContract_resuelveContractPendingData() {
		Request request = requestConId(6L, RequestEntityType.CONTRACT);
		request.setProject(proyectoConNombre("Edificio Centro"));
		request.setStatus(statusConCodigo("APPROVED"));
		request.setPendingData("{}");
		when(requestRepository.findById(6L)).thenReturn(Optional.of(request));
		when(handlerRegistry.get(RequestEntityType.CONTRACT)).thenReturn(contractRequestHandler);
		when(approvalRepository.findByRequest_IdOrderByCreatedAtAsc(6L)).thenReturn(List.of());
		ContractPendingDataResolvedDto resuelto = mock(ContractPendingDataResolvedDto.class);
		when(contractPendingDataResolver.resolve("{}")).thenReturn(resuelto);

		RequestDetailDto detalle = requestService.getRequestDetail(6L);

		assertThat(detalle.getContractPendingData()).isEqualTo(resuelto);
	}

	@Test
	void getRequestDetail_conIdInexistente_lanzaResourceNotFound() {
		when(requestRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> requestService.getRequestDetail(99L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	private void autenticarCon(String autoridad) {
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken("usuario", null, List.of(new SimpleGrantedAuthority(autoridad))));
	}

	private Project proyectoConNombre(String name) {
		Project project = new Project();
		project.setName(name);
		return project;
	}

	private Request requestConId(Long id, RequestEntityType entityType) {
		Request request = new Request();
		request.setId(id);
		request.setEntityType(entityType);
		request.setAction(RequestAction.CREATE);
		return request;
	}

	private PlatformStatus statusConCodigo(String code) {
		PlatformStatus status = new PlatformStatus();
		status.setCode(code);
		return status;
	}

	private RequestStaffDataProjection staffDataProjection(
		Long contractId, String firstName, String paternalLastName, String maternalLastName,
		String identificationNumber, String jobTitleName
	) {
		return new RequestStaffDataProjection() {
			@Override
			public Long getContractId() {
				return contractId;
			}

			@Override
			public IdentificationType getStaffIdentificationType() {
				return IdentificationType.RUT;
			}

			@Override
			public String getStaffIdentificationNumber() {
				return identificationNumber;
			}

			@Override
			public String getStaffFirstName() {
				return firstName;
			}

			@Override
			public String getStaffPaternalLastName() {
				return paternalLastName;
			}

			@Override
			public String getStaffMaternalLastName() {
				return maternalLastName;
			}

			@Override
			public String getJobTitleName() {
				return jobTitleName;
			}
		};
	}

	private DecideRequestDto decisionAprobada() {
		DecideRequestDto dto = new DecideRequestDto();
		dto.setDecision(ApprovalDecision.APPROVED);
		return dto;
	}
}
