package com.promaty.rrhh.services.annex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.annex.AnnexDetailDto;
import com.promaty.rrhh.dto.annex.AnnexFilterParams;
import com.promaty.rrhh.dto.annex.AnnexListDto;
import com.promaty.rrhh.dto.annex.UpdateAnnexDto;
import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AttachmentEntityType;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.AnnexRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.services.annex.business.AnnexAttachmentRules;
import com.promaty.rrhh.services.annex.business.builder.AnnexQueryBuilder;
import com.promaty.rrhh.services.annex.business.builder.AnnexSortField;
import com.promaty.rrhh.services.annex.business.builder.UpdateAnnexBuilder;
import com.promaty.rrhh.services.annex.business.mapper.AnnexMapper;
import com.promaty.rrhh.services.annex.business.validation.AnnexValidation;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.shared.ProjectAccessSpecification;
import com.promaty.rrhh.services.shared.RequestApprovalActionsResolver;
import com.promaty.rrhh.services.shared.RequestEditPolicy;
import com.promaty.rrhh.services.shared.SyncRetryAction;
import com.promaty.rrhh.services.shared.attachment.AttachmentOwner;
import com.promaty.rrhh.services.shared.attachment.AttachmentService;
import com.promaty.rrhh.services.shared.sort.ListSorting;
import com.promaty.rrhh.services.shared.sort.SortedQuery;

@Service
public class AnnexServiceImpl implements AnnexService {

	private static final String NOT_FOUND_MESSAGE = "Anexo no encontrado.";
	private static final String PERMISSION_APPROVE = "annex.approve";
	private static final String PERMISSION_VALIDATE = "annex.validate";
	private static final String PERMISSION_UPDATE = "annex.update";
	private static final String SUBMODULE_ANNEX = "annex";

	private final AnnexRepository annexRepository;
	private final RequestRepository requestRepository;
	private final AnnexValidation annexValidation;
	private final UpdateAnnexBuilder updateAnnexBuilder;
	private final RequestApprovalActionsResolver actionsResolver;
	private final AttachmentService attachmentService;
	private final RequestEditPolicy requestEditPolicy;

	public AnnexServiceImpl(
		AnnexRepository annexRepository,
		RequestRepository requestRepository,
		AnnexValidation annexValidation,
		UpdateAnnexBuilder updateAnnexBuilder,
		RequestApprovalActionsResolver actionsResolver,
		AttachmentService attachmentService,
		RequestEditPolicy requestEditPolicy
	) {
		this.annexRepository = annexRepository;
		this.requestRepository = requestRepository;
		this.annexValidation = annexValidation;
		this.updateAnnexBuilder = updateAnnexBuilder;
		this.actionsResolver = actionsResolver;
		this.attachmentService = attachmentService;
		this.requestEditPolicy = requestEditPolicy;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AnnexListDto> listAnnexes(AnnexFilterParams filters, Pageable pageable) {
		Specification<Annex> specification = AnnexQueryBuilder.fromFilters(filters)
			.and(ProjectAccessSpecification.onProject());
		SortedQuery<Annex> sorted = ListSorting.apply(specification, pageable, AnnexSortField.class);
		Page<AnnexListDto> page = annexRepository.findAll(sorted.specification(), sorted.pageable()).map(AnnexMapper::toListDto);

		Map<Long, Request> requestsByAnnexId = findRequestsByAnnexIds(page.map(AnnexListDto::getId).getContent());
		Set<String> authorities = CurrentUserAuthorities.get();
		return page.map(dto -> {
			Request request = requestsByAnnexId.get(dto.getId());
			dto.setRequestId(request == null ? null : request.getId());
			dto.setActions(resolveActions(request, dto.getStatusCode(), authorities));
			return dto;
		});
	}

	@Override
	@Transactional(readOnly = true)
	public AnnexDetailDto getAnnexDetail(Long id) {
		Annex annex = findById(id);
		AnnexDetailDto dto = AnnexMapper.toDetailDto(annex);
		Request request = requestRepository.findByEntityTypeAndEntityId(RequestEntityType.ANNEX, id).orElse(null);
		dto.setRequestId(request == null ? null : request.getId());
		dto.setActions(resolveActions(request, annex.getStatus().getCode(), CurrentUserAuthorities.get()));
		dto.setSyncErrorMessage(request == null ? null : request.getSyncErrorMessage());
		AttachmentOwner owner = attachmentOwner(id);
		dto.setAttachments(attachmentService.list(owner));
		dto.setAttachmentRequirements(attachmentService.requirements(owner, AnnexAttachmentRules.rules()));
		return dto;
	}

	@Override
	@Transactional
	public void updateAnnex(Long id, UpdateAnnexDto dto, List<MultipartFile> files) {
		Annex annex = findById(id);
		Request request = requestEditPolicy.requireEditable(RequestEntityType.ANNEX, id);
		annexValidation.validateUpdate(annex, dto);
		updateAnnexBuilder.applyUpdate(annex, dto);
		attachmentService.sync(attachmentOwner(id), AnnexAttachmentRules.rules(), dto.getAttachments(), files);
		requestEditPolicy.replacePendingData(request, AnnexMapper.toPendingData(annex));
		if (requestEditPolicy.returnToPendingApproval(request)) {
			annex.setStatus(requestEditPolicy.pendingApprovalStatus(SUBMODULE_ANNEX));
		}
		annexRepository.save(annex);
	}

	private AttachmentOwner attachmentOwner(Long annexId) {
		return new AttachmentOwner(AttachmentEntityType.ANNEX, annexId);
	}

	private Annex findById(Long id) {
		return annexRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
	}

	private Map<Long, Request> findRequestsByAnnexIds(List<Long> annexIds) {
		if (annexIds.isEmpty()) {
			return Map.of();
		}
		return requestRepository.findByEntityTypeAndEntityIdIn(RequestEntityType.ANNEX, annexIds).stream()
			.collect(Collectors.toMap(Request::getEntityId, Function.identity()));
	}

	private List<Action> resolveActions(Request request, String annexStatusCode, Set<String> authorities) {
		if (request == null) {
			return List.of();
		}
		List<Action> actions = new ArrayList<>(actionsResolver.resolve(
			request.getStatus().getCode(), authorities, PERMISSION_APPROVE, PERMISSION_VALIDATE
		));
		if (requestEditPolicy.isEditable(request) && authorities.contains(PERMISSION_UPDATE)) {
			actions.add(Action.UPDATE);
		}
		if (SyncRetryAction.isAvailable(annexStatusCode, authorities, PERMISSION_VALIDATE)) {
			actions.add(Action.RETRY_SYNC);
		}
		return actions;
	}
}
