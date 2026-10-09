package com.promaty.rrhh.services.request;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.request.DecideRequestDto;
import com.promaty.rrhh.entity.Approval;
import com.promaty.rrhh.entity.ApprovalLevel;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ApprovalRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.services.request.business.builder.CreateRequestBuilder;
import com.promaty.rrhh.services.request.business.builder.RequestRelationsResolver;
import com.promaty.rrhh.services.request.business.validation.RequestDecisionValidation;
import com.promaty.rrhh.services.request.business.validation.RequestValidation;
import com.promaty.rrhh.services.request.handler.RequestHandlerRegistry;
import com.promaty.rrhh.services.shared.CurrentUserId;

@Service
public class RequestServiceImpl implements RequestService {

	private static final String CODE_APPROVED = "APPROVED";

	private final RequestRepository requestRepository;
	private final ApprovalRepository approvalRepository;
	private final RequestValidation requestValidation;
	private final RequestDecisionValidation requestDecisionValidation;
	private final CreateRequestBuilder createRequestBuilder;
	private final RequestRelationsResolver relationsResolver;
	private final RequestHandlerRegistry handlerRegistry;

	public RequestServiceImpl(
		RequestRepository requestRepository,
		ApprovalRepository approvalRepository,
		RequestValidation requestValidation,
		RequestDecisionValidation requestDecisionValidation,
		CreateRequestBuilder createRequestBuilder,
		RequestRelationsResolver relationsResolver,
		RequestHandlerRegistry handlerRegistry
	) {
		this.requestRepository = requestRepository;
		this.approvalRepository = approvalRepository;
		this.requestValidation = requestValidation;
		this.requestDecisionValidation = requestDecisionValidation;
		this.createRequestBuilder = createRequestBuilder;
		this.relationsResolver = relationsResolver;
		this.handlerRegistry = handlerRegistry;
	}

	@Override
	@Transactional
	public Long createRequest(CreateRequestDto dto) {
		requestValidation.validateCreate(dto);
		Request request = createRequestBuilder.build(dto);
		return requestRepository.save(request).getId();
	}

	@Override
	@Transactional
	public void decideRequest(Long id, DecideRequestDto dto) {
		Request request = requestRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("La solicitud indicada no existe."));

		ApprovalLevel nivelDecidido = requestDecisionValidation.validateDecision(request, dto);
		registrarApproval(request, dto, nivelDecidido);

		PlatformStatus siguienteStatus = relationsResolver.resolveNextStatus(nivelDecidido, dto.getDecision());
		request.setStatus(siguienteStatus);
		if (CODE_APPROVED.equals(siguienteStatus.getCode())) {
			Long entityId = handlerRegistry.get(request.getEntityType()).apply(request);
			request.setEntityId(entityId);
		}
		requestRepository.save(request);
	}

	private void registrarApproval(Request request, DecideRequestDto dto, ApprovalLevel nivelDecidido) {
		Approval approval = new Approval();
		approval.setRequest(request);
		approval.setApproverUserId(CurrentUserId.get());
		approval.setLevel(nivelDecidido);
		approval.setDecision(dto.getDecision());
		approval.setRejectionReason(dto.getRejectionReason());
		approval.setComment(dto.getComment());
		approvalRepository.save(approval);
	}
}
