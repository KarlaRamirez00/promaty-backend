package com.promaty.rrhh.services.request;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.request.CreatedRequestDto;
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
import com.promaty.rrhh.repository.AnnexRepository;
import com.promaty.rrhh.repository.ApprovalRepository;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.RequestRejectionReasonRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.repository.projection.RequestStaffDataProjection;
import com.promaty.rrhh.services.request.business.builder.CreateRequestBuilder;
import com.promaty.rrhh.services.request.business.builder.RequestQueryBuilder;
import com.promaty.rrhh.services.request.business.builder.RequestRelationsResolver;
import com.promaty.rrhh.services.request.business.builder.RequestSortField;
import com.promaty.rrhh.services.request.business.mapper.ContractPendingDataResolver;
import com.promaty.rrhh.services.request.business.mapper.RequestMapper;
import com.promaty.rrhh.services.request.business.mapper.TerminationPendingDataResolver;
import com.promaty.rrhh.services.request.business.validation.RequestDecisionValidation;
import com.promaty.rrhh.services.request.business.validation.RequestValidation;
import com.promaty.rrhh.services.request.handler.RequestHandler;
import com.promaty.rrhh.services.request.handler.RequestHandlerRegistry;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.shared.CurrentUserId;
import com.promaty.rrhh.services.shared.ProjectAccessSpecification;
import com.promaty.rrhh.services.shared.RequestApprovalActionsResolver;
import com.promaty.rrhh.services.shared.attachment.AttachmentOwner;
import com.promaty.rrhh.services.shared.attachment.AttachmentService;
import com.promaty.rrhh.services.shared.sort.ListSorting;
import com.promaty.rrhh.services.shared.sort.SortedQuery;

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
	private final AnnexRepository annexRepository;
	private final RequestValidation requestValidation;
	private final RequestDecisionValidation requestDecisionValidation;
	private final CreateRequestBuilder createRequestBuilder;
	private final RequestRelationsResolver relationsResolver;
	private final RequestHandlerRegistry handlerRegistry;
	private final ContractPendingDataResolver contractPendingDataResolver;
	private final TerminationPendingDataResolver terminationPendingDataResolver;
	private final RequestApprovalActionsResolver actionsResolver;
	private final AttachmentService attachmentService;

	public RequestServiceImpl(
		RequestRepository requestRepository,
		ApprovalRepository approvalRepository,
		RequestRejectionReasonRepository requestRejectionReasonRepository,
		ContractRepository contractRepository,
		TerminationRepository terminationRepository,
		AnnexRepository annexRepository,
		RequestValidation requestValidation,
		RequestDecisionValidation requestDecisionValidation,
		CreateRequestBuilder createRequestBuilder,
		RequestRelationsResolver relationsResolver,
		RequestHandlerRegistry handlerRegistry,
		ContractPendingDataResolver contractPendingDataResolver,
		TerminationPendingDataResolver terminationPendingDataResolver,
		RequestApprovalActionsResolver actionsResolver,
		AttachmentService attachmentService
	) {
		this.requestRepository = requestRepository;
		this.approvalRepository = approvalRepository;
		this.requestRejectionReasonRepository = requestRejectionReasonRepository;
		this.contractRepository = contractRepository;
		this.terminationRepository = terminationRepository;
		this.annexRepository = annexRepository;
		this.requestValidation = requestValidation;
		this.requestDecisionValidation = requestDecisionValidation;
		this.createRequestBuilder = createRequestBuilder;
		this.relationsResolver = relationsResolver;
		this.handlerRegistry = handlerRegistry;
		this.contractPendingDataResolver = contractPendingDataResolver;
		this.terminationPendingDataResolver = terminationPendingDataResolver;
		this.actionsResolver = actionsResolver;
		this.attachmentService = attachmentService;
	}

	@Override
	@Transactional
	public CreatedRequestDto createRequest(CreateRequestDto dto, List<MultipartFile> files) {
		requestValidation.validateCreate(dto);
		Request request = createRequestBuilder.build(dto);
		RequestHandler handler = handlerRegistry.get(request.getEntityType());
		Long entityId = handler.create(request);
		request.setEntityId(entityId);
		Long requestId = requestRepository.save(request).getId();
		attachmentService.sync(
			new AttachmentOwner(handler.attachmentEntityType(), entityId),
			handler.attachmentRules(entityId),
			dto.getAttachments() == null ? List.of() : dto.getAttachments(),
			files
		);
		return new CreatedRequestDto(requestId, entityId);
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
		SortedQuery<Request> sorted = ListSorting.apply(especificacion, pageable, RequestSortField.class);
		Page<Request> pagina = requestRepository.findAll(sorted.specification(), sorted.pageable());

		Map<RequestEntityType, Map<Long, RequestStaffDataProjection>> staffDataByType = Map.of(
			RequestEntityType.CONTRACT, indexById(idsDeEntidad(pagina, RequestEntityType.CONTRACT), contractRepository::findStaffDataByContractIds),
			RequestEntityType.TERMINATION, indexById(idsDeEntidad(pagina, RequestEntityType.TERMINATION), terminationRepository::findStaffDataByTerminationIds),
			RequestEntityType.ANNEX, indexById(idsDeEntidad(pagina, RequestEntityType.ANNEX), annexRepository::findStaffDataByAnnexIds)
		);

		return pagina
			.map(RequestMapper::toListDto)
			.map(dto -> withStaffData(dto, staffDataByType.get(dto.getEntityType()).get(dto.getEntityId())))
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

	private Map<Long, RequestStaffDataProjection> indexById(
		List<Long> entityIds,
		Function<List<Long>, List<RequestStaffDataProjection>> finder
	) {
		if (entityIds.isEmpty()) {
			return Map.of();
		}
		return finder.apply(entityIds).stream()
			.collect(Collectors.toMap(RequestStaffDataProjection::getEntityId, Function.identity()));
	}

	private RequestListDto withStaffData(RequestListDto dto, RequestStaffDataProjection datos) {
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
