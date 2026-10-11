package com.promaty.rrhh.services.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.request.DecideRequestDto;
import com.promaty.rrhh.dto.request.RequestCountersDto;
import com.promaty.rrhh.dto.request.RequestDetailDto;
import com.promaty.rrhh.dto.request.RequestFilterParams;
import com.promaty.rrhh.dto.request.RequestListDto;
import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.Approval;
import com.promaty.rrhh.entity.ApprovalLevel;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.entity.RequestRejectionReason;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ApprovalRepository;
import com.promaty.rrhh.repository.RequestRejectionReasonRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.services.request.business.builder.CreateRequestBuilder;
import com.promaty.rrhh.services.request.business.builder.RequestQueryBuilder;
import com.promaty.rrhh.services.request.business.builder.RequestRelationsResolver;
import com.promaty.rrhh.services.request.business.mapper.ContractPendingDataResolver;
import com.promaty.rrhh.services.request.business.mapper.RequestMapper;
import com.promaty.rrhh.services.request.business.validation.RequestDecisionValidation;
import com.promaty.rrhh.services.request.business.validation.RequestValidation;
import com.promaty.rrhh.services.request.handler.RequestHandlerRegistry;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.shared.CurrentUserId;
import com.promaty.rrhh.services.shared.ProjectAccessSpecification;

@Service
public class RequestServiceImpl implements RequestService {

	private static final String CODE_APPROVED = "APPROVED";
	private static final String CODE_PENDING_APPROVAL = "PENDING_APPROVAL";
	private static final String CODE_PENDING_VALIDATION = "PENDING_VALIDATION";
	private static final String PERMISO_APROBAR = "contract.approve";
	private static final String PERMISO_VALIDAR = "contract.validate";
	private static final String NO_ENCONTRADA = "La solicitud indicada no existe.";

	private final RequestRepository requestRepository;
	private final ApprovalRepository approvalRepository;
	private final RequestRejectionReasonRepository requestRejectionReasonRepository;
	private final RequestValidation requestValidation;
	private final RequestDecisionValidation requestDecisionValidation;
	private final CreateRequestBuilder createRequestBuilder;
	private final RequestRelationsResolver relationsResolver;
	private final RequestHandlerRegistry handlerRegistry;
	private final ContractPendingDataResolver contractPendingDataResolver;

	public RequestServiceImpl(
		RequestRepository requestRepository,
		ApprovalRepository approvalRepository,
		RequestRejectionReasonRepository requestRejectionReasonRepository,
		RequestValidation requestValidation,
		RequestDecisionValidation requestDecisionValidation,
		CreateRequestBuilder createRequestBuilder,
		RequestRelationsResolver relationsResolver,
		RequestHandlerRegistry handlerRegistry,
		ContractPendingDataResolver contractPendingDataResolver
	) {
		this.requestRepository = requestRepository;
		this.approvalRepository = approvalRepository;
		this.requestRejectionReasonRepository = requestRejectionReasonRepository;
		this.requestValidation = requestValidation;
		this.requestDecisionValidation = requestDecisionValidation;
		this.createRequestBuilder = createRequestBuilder;
		this.relationsResolver = relationsResolver;
		this.handlerRegistry = handlerRegistry;
		this.contractPendingDataResolver = contractPendingDataResolver;
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
		Set<String> autoridades = CurrentUserAuthorities.get();
		return requestRepository.findAll(especificacion, pageable)
			.map(RequestMapper::toListDto)
			.map(dto -> conAcciones(dto, autoridades));
	}

	@Override
	@Transactional(readOnly = true)
	public RequestDetailDto getRequestDetail(Long id) {
		Request request = buscarPorId(id);
		List<Approval> approvals = approvalRepository.findByRequest_IdOrderByCreatedAtAsc(id);
		RequestDetailDto dto = RequestMapper.toDetailDto(request, approvals);
		if (request.getEntityType() == RequestEntityType.CONTRACT) {
			dto.setContractPendingData(contractPendingDataResolver.resolve(request.getPendingData()));
		}
		dto.setActions(resolveRowActions(dto.getStatus().getCode(), CurrentUserAuthorities.get()));
		return dto;
	}

	@Override
	@Transactional(readOnly = true)
	public RequestCountersDto getCounters(RequestEntityType entityType) {
		return new RequestCountersDto(
			contarPorStatus(CODE_PENDING_APPROVAL, entityType),
			contarPorStatus(CODE_PENDING_VALIDATION, entityType)
		);
	}

	private long contarPorStatus(String statusCode, RequestEntityType entityType) {
		Specification<Request> especificacion = ProjectAccessSpecification.<Request>onProject()
			.and((root, query, cb) -> cb.equal(root.get("status").get("code"), statusCode));
		if (entityType != null) {
			especificacion = especificacion.and((root, query, cb) -> cb.equal(root.get("entityType"), entityType));
		}
		return requestRepository.count(especificacion);
	}

	private Request buscarPorId(Long id) {
		return requestRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADA));
	}

	private RequestListDto conAcciones(RequestListDto dto, Set<String> autoridades) {
		dto.setActions(resolveRowActions(dto.getStatus().getCode(), autoridades));
		return dto;
	}

	private List<Action> resolveRowActions(String statusCode, Set<String> autoridades) {
		List<Action> actions = new ArrayList<>();
		if (CODE_PENDING_APPROVAL.equals(statusCode) && autoridades.contains(PERMISO_APROBAR)) {
			actions.add(Action.APPROVE);
		} else if (CODE_PENDING_VALIDATION.equals(statusCode) && autoridades.contains(PERMISO_VALIDAR)) {
			actions.add(Action.VALIDATE);
		}
		return actions;
	}

	private void registrarApproval(Request request, DecideRequestDto dto, ApprovalLevel nivelDecidido) {
		Approval approval = new Approval();
		approval.setRequest(request);
		approval.setApproverUserId(CurrentUserId.get());
		approval.setLevel(nivelDecidido);
		approval.setDecision(dto.getDecision());
		approval.setRejectionReason(resolveRejectionReason(dto.getRejectionReasonId()));
		approval.setComment(dto.getComment());
		approvalRepository.save(approval);
	}

	private RequestRejectionReason resolveRejectionReason(Long rejectionReasonId) {
		return rejectionReasonId == null ? null : requestRejectionReasonRepository.findById(rejectionReasonId).orElse(null);
	}
}
