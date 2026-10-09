package com.promaty.rrhh.services.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.request.DecideRequestDto;
import com.promaty.rrhh.entity.ApprovalDecision;
import com.promaty.rrhh.entity.ApprovalLevel;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.repository.ApprovalRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.services.request.business.builder.CreateRequestBuilder;
import com.promaty.rrhh.services.request.business.builder.RequestRelationsResolver;
import com.promaty.rrhh.services.request.business.validation.RequestDecisionValidation;
import com.promaty.rrhh.services.request.business.validation.RequestValidation;
import com.promaty.rrhh.services.request.handler.RequestHandler;
import com.promaty.rrhh.services.request.handler.RequestHandlerRegistry;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

	@Mock
	private RequestRepository requestRepository;
	@Mock
	private ApprovalRepository approvalRepository;
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

	@InjectMocks
	private RequestServiceImpl requestService;

	@Test
	void createRequest_validaArmaYGuarda_retornaIdGenerado() {
		CreateRequestDto dto = new CreateRequestDto();
		Request request = new Request();
		Request guardado = new Request();
		guardado.setId(10L);

		when(createRequestBuilder.build(dto)).thenReturn(request);
		when(requestRepository.save(request)).thenReturn(guardado);

		Long id = requestService.createRequest(dto);

		assertThat(id).isEqualTo(10L);
		verify(requestValidation).validateCreate(dto);
	}

	@Test
	void decideRequest_aprobarEnPendingApproval_avanzaAPendingValidationSinInvocarHandler() {
		Request request = requestConId(1L, RequestEntityType.CONTRACT);
		DecideRequestDto dto = decisionAprobada();
		when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
		when(requestDecisionValidation.validateDecision(request, dto)).thenReturn(ApprovalLevel.PROJECT_MANAGER);
		PlatformStatus pendingValidation = statusConCodigo("PENDING_VALIDATION");
		when(relationsResolver.resolveNextStatus(ApprovalLevel.PROJECT_MANAGER, ApprovalDecision.APPROVED))
			.thenReturn(pendingValidation);

		requestService.decideRequest(1L, dto);

		assertThat(request.getStatus()).isEqualTo(pendingValidation);
		assertThat(request.getEntityId()).isNull();
		verify(handlerRegistry, never()).get(any());
		verify(approvalRepository).save(any());
		verify(requestRepository).save(request);
	}

	@Test
	void decideRequest_aprobarEnPendingValidation_invocaHandlerYGuardaEntityId() {
		Request request = requestConId(2L, RequestEntityType.CONTRACT);
		DecideRequestDto dto = decisionAprobada();
		when(requestRepository.findById(2L)).thenReturn(Optional.of(request));
		when(requestDecisionValidation.validateDecision(request, dto)).thenReturn(ApprovalLevel.HR);
		PlatformStatus approved = statusConCodigo("APPROVED");
		when(relationsResolver.resolveNextStatus(ApprovalLevel.HR, ApprovalDecision.APPROVED)).thenReturn(approved);
		when(handlerRegistry.get(RequestEntityType.CONTRACT)).thenReturn(contractRequestHandler);
		when(contractRequestHandler.apply(request)).thenReturn(55L);

		requestService.decideRequest(2L, dto);

		assertThat(request.getStatus()).isEqualTo(approved);
		assertThat(request.getEntityId()).isEqualTo(55L);
	}

	@Test
	void decideRequest_rechazar_noInvocaHandler() {
		Request request = requestConId(3L, RequestEntityType.CONTRACT);
		DecideRequestDto dto = new DecideRequestDto();
		dto.setDecision(ApprovalDecision.REJECTED);
		dto.setRejectionReason("Motivo");
		when(requestRepository.findById(3L)).thenReturn(Optional.of(request));
		when(requestDecisionValidation.validateDecision(request, dto)).thenReturn(ApprovalLevel.PROJECT_MANAGER);
		PlatformStatus rejected = statusConCodigo("REJECTED");
		when(relationsResolver.resolveNextStatus(ApprovalLevel.PROJECT_MANAGER, ApprovalDecision.REJECTED))
			.thenReturn(rejected);

		requestService.decideRequest(3L, dto);

		assertThat(request.getStatus()).isEqualTo(rejected);
		verify(handlerRegistry, never()).get(any());
	}

	private Request requestConId(Long id, RequestEntityType entityType) {
		Request request = new Request();
		request.setId(id);
		request.setEntityType(entityType);
		return request;
	}

	private PlatformStatus statusConCodigo(String code) {
		PlatformStatus status = new PlatformStatus();
		status.setCode(code);
		return status;
	}

	private DecideRequestDto decisionAprobada() {
		DecideRequestDto dto = new DecideRequestDto();
		dto.setDecision(ApprovalDecision.APPROVED);
		return dto;
	}
}
