package com.promaty.rrhh.services.request;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.request.DecideRequestDto;
import com.promaty.rrhh.dto.request.RequestDetailDto;
import com.promaty.rrhh.dto.request.RequestFilterParams;
import com.promaty.rrhh.dto.request.RequestListDto;
import com.promaty.rrhh.entity.Approval;
import com.promaty.rrhh.entity.ApprovalLevel;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ApprovalRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.services.request.business.builder.CreateRequestBuilder;
import com.promaty.rrhh.services.request.business.builder.RequestQueryBuilder;
import com.promaty.rrhh.services.request.business.builder.RequestRelationsResolver;
import com.promaty.rrhh.services.request.business.mapper.RequestMapper;
import com.promaty.rrhh.services.request.business.validation.RequestDecisionValidation;
import com.promaty.rrhh.services.request.business.validation.RequestValidation;
import com.promaty.rrhh.services.request.handler.RequestHandlerRegistry;
import com.promaty.rrhh.services.shared.CurrentUserId;
import com.promaty.rrhh.services.shared.ProjectAccessSpecification;

@Service
public class RequestServiceImpl implements RequestService {

	private static final String CODE_APPROVED = "APPROVED";
	private static final String NO_ENCONTRADA = "La solicitud indicada no existe.";

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
		Request request = buscarPorId(id);

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

	@Override
	@Transactional(readOnly = true)
	public Page<RequestListDto> listRequests(RequestFilterParams filters, Pageable pageable) {
		Specification<Request> especificacion = RequestQueryBuilder.fromFilters(filters)
			.and(ProjectAccessSpecification.onProject());
		return requestRepository.findAll(especificacion, pageable)
			.map(RequestMapper::toListDto)
			.map(this::conAcciones);
	}

	@Override
	@Transactional(readOnly = true)
	public RequestDetailDto getRequestDetail(Long id) {
		Request request = buscarPorId(id);
		List<Approval> approvals = approvalRepository.findByRequest_IdOrderByCreatedAtAsc(id);
		RequestDetailDto dto = RequestMapper.toDetailDto(request, approvals);
		dto.setActions(List.of());
		return dto;
	}

	private Request buscarPorId(Long id) {
		return requestRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADA));
	}

	private RequestListDto conAcciones(RequestListDto dto) {
		dto.setActions(List.of());
		return dto;
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
