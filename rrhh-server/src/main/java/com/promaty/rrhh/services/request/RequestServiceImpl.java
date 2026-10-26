package com.promaty.rrhh.services.request;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.request.DecideRequestDto;
import com.promaty.rrhh.dto.request.RequestCountersDto;
import com.promaty.rrhh.dto.request.RequestDetailDto;
import com.promaty.rrhh.dto.request.RequestFilterParams;
import com.promaty.rrhh.dto.request.RequestListDto;
import com.promaty.rrhh.entity.Approval;
import com.promaty.rrhh.entity.ApprovalLevel;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.entity.RequestRejectionReason;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ApprovalRepository;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.RequestRejectionReasonRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.repository.projection.RequestStaffDataProjection;
import com.promaty.rrhh.services.request.business.builder.CreateRequestBuilder;
import com.promaty.rrhh.services.request.business.builder.RequestQueryBuilder;
import com.promaty.rrhh.services.request.business.builder.RequestRelationsResolver;
import com.promaty.rrhh.services.request.business.mapper.ContractPendingDataResolver;
import com.promaty.rrhh.services.request.business.mapper.RequestMapper;
import com.promaty.rrhh.services.request.business.mapper.TerminationPendingDataResolver;
import com.promaty.rrhh.services.request.business.validation.RequestDecisionValidation;
import com.promaty.rrhh.services.request.business.validation.RequestValidation;
import com.promaty.rrhh.services.request.handler.RequestHandlerRegistry;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.shared.CurrentUserId;
import com.promaty.rrhh.services.shared.ProjectAccessSpecification;
import com.promaty.rrhh.services.shared.RequestApprovalActionsResolver;

@Service
public class RequestServiceImpl implements RequestService {

	private static final String CODE_PENDING_APPROVAL = "PENDING_APPROVAL";
	private static final String CODE_PENDING_VALIDATION = "PENDING_VALIDATION";
	private static final String NO_ENCONTRADA = "La solicitud indicada no existe.";

	private final RequestRepository requestRepository;
	private final ApprovalRepository approvalRepository;
	private final RequestRejectionReasonRepository requestRejectionReasonRepository;
	private final ContractRepository contractRepository;
	private final TerminationRepository terminationRepository;
	private final RequestValidation requestValidation;
	private final RequestDecisionValidation requestDecisionValidation;
	private final CreateRequestBuilder createRequestBuilder;
	private final RequestRelationsResolver relationsResolver;
	private final RequestHandlerRegistry handlerRegistry;
	private final ContractPendingDataResolver contractPendingDataResolver;
	private final TerminationPendingDataResolver terminationPendingDataResolver;
	private final RequestApprovalActionsResolver actionsResolver;

	public RequestServiceImpl(
		RequestRepository requestRepository,
		ApprovalRepository approvalRepository,
		RequestRejectionReasonRepository requestRejectionReasonRepository,
		ContractRepository contractRepository,
		TerminationRepository terminationRepository,
		RequestValidation requestValidation,
		RequestDecisionValidation requestDecisionValidation,
		CreateRequestBuilder createRequestBuilder,
		RequestRelationsResolver relationsResolver,
		RequestHandlerRegistry handlerRegistry,
		ContractPendingDataResolver contractPendingDataResolver,
		TerminationPendingDataResolver terminationPendingDataResolver,
		RequestApprovalActionsResolver actionsResolver
	) {
		this.requestRepository = requestRepository;
		this.approvalRepository = approvalRepository;
		this.requestRejectionReasonRepository = requestRejectionReasonRepository;
		this.contractRepository = contractRepository;
		this.terminationRepository = terminationRepository;
		this.requestValidation = requestValidation;
		this.requestDecisionValidation = requestDecisionValidation;
		this.createRequestBuilder = createRequestBuilder;
		this.relationsResolver = relationsResolver;
		this.handlerRegistry = handlerRegistry;
		this.contractPendingDataResolver = contractPendingDataResolver;
		this.terminationPendingDataResolver = terminationPendingDataResolver;
		this.actionsResolver = actionsResolver;
	}

	@Override
	@Transactional
	public Long createRequest(CreateRequestDto dto) {
		requestValidation.validateCreate(dto);
		Request request = createRequestBuilder.build(dto);
		Long entityId = handlerRegistry.get(request.getEntityType()).create(request);
		request.setEntityId(entityId);
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
		handlerRegistry.get(request.getEntityType()).applyDecision(request, nivelDecidido, dto.getDecision());
		requestRepository.save(request);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<RequestListDto> listRequests(RequestFilterParams filters, Pageable pageable) {
		List<RequestEntityType> tiposLegibles = handlerRegistry.entityTypesReadableBy(CurrentUserAuthorities.get());
		Specification<Request> especificacion = RequestQueryBuilder.fromFilters(filters)
			.and(RequestQueryBuilder.restrictToEntityTypes(tiposLegibles))
			.and(ProjectAccessSpecification.onProject());
		Page<Request> pagina = requestRepository.findAll(especificacion, pageable);

		Map<Long, RequestStaffDataProjection> datosDeStaffPorContrato = resolverDatosDeStaffPorContrato(
			idsDeEntidad(pagina, RequestEntityType.CONTRACT)
		);
		Map<Long, RequestStaffDataProjection> datosDeStaffPorTermination = resolverDatosDeStaffPorTermination(
			idsDeEntidad(pagina, RequestEntityType.TERMINATION)
		);

		return pagina
			.map(RequestMapper::toListDto)
			.map(dto -> conDatosDeStaff(dto, datosDeStaffPorContrato, datosDeStaffPorTermination))
			.map(this::sinAccionesDeDecision);
	}

	private List<Long> idsDeEntidad(Page<Request> pagina, RequestEntityType entityType) {
		return pagina.getContent().stream()
			.filter(request -> request.getEntityType() == entityType)
			.map(Request::getEntityId)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public RequestDetailDto getRequestDetail(Long id) {
		Request request = buscarPorId(id);
		var handler = handlerRegistry.get(request.getEntityType());
		if (!CurrentUserAuthorities.get().contains(handler.readPermission())) {
			throw new AccessDeniedException("No tiene el permiso requerido para ver este tipo de solicitud.");
		}
		List<Approval> approvals = approvalRepository.findByRequest_IdOrderByCreatedAtAsc(id);
		RequestDetailDto dto = RequestMapper.toDetailDto(request, approvals);
		if (request.getEntityType() == RequestEntityType.CONTRACT) {
			dto.setContractPendingData(contractPendingDataResolver.resolve(request.getPendingData()));
		}
		if (request.getEntityType() == RequestEntityType.TERMINATION) {
			dto.setTerminationPendingData(terminationPendingDataResolver.resolve(request.getPendingData()));
		}
		dto.setActions(actionsResolver.resolve(
			dto.getStatus().getCode(), CurrentUserAuthorities.get(), handler.approvePermission(), handler.validatePermission()
		));
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

	private RequestListDto sinAccionesDeDecision(RequestListDto dto) {
		dto.setActions(List.of());
		return dto;
	}

	private Map<Long, RequestStaffDataProjection> resolverDatosDeStaffPorContrato(List<Long> contractIds) {
		if (contractIds.isEmpty()) {
			return Map.of();
		}
		return contractRepository.findStaffDataByContractIds(contractIds).stream()
			.collect(Collectors.toMap(RequestStaffDataProjection::getEntityId, datos -> datos));
	}

	private Map<Long, RequestStaffDataProjection> resolverDatosDeStaffPorTermination(List<Long> terminationIds) {
		if (terminationIds.isEmpty()) {
			return Map.of();
		}
		return terminationRepository.findStaffDataByTerminationIds(terminationIds).stream()
			.collect(Collectors.toMap(RequestStaffDataProjection::getEntityId, datos -> datos));
	}

	private RequestListDto conDatosDeStaff(
		RequestListDto dto,
		Map<Long, RequestStaffDataProjection> datosDeStaffPorContrato,
		Map<Long, RequestStaffDataProjection> datosDeStaffPorTermination
	) {
		Map<Long, RequestStaffDataProjection> mapaCorrespondiente = dto.getEntityType() == RequestEntityType.TERMINATION
			? datosDeStaffPorTermination
			: datosDeStaffPorContrato;
		RequestStaffDataProjection datos = mapaCorrespondiente.get(dto.getEntityId());
		if (datos != null) {
			dto.setStaffIdentificationType(datos.getStaffIdentificationType());
			dto.setStaffIdentificationNumber(datos.getStaffIdentificationNumber());
			dto.setStaffFirstName(datos.getStaffFirstName());
			dto.setStaffPaternalLastName(datos.getStaffPaternalLastName());
			dto.setStaffMaternalLastName(datos.getStaffMaternalLastName());
			dto.setStaffPosition(datos.getJobTitleName());
		}
		return dto;
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
