package com.promaty.rrhh.services.termination;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.dto.termination.AnnulTerminationDto;
import com.promaty.rrhh.dto.termination.TerminationDetailDto;
import com.promaty.rrhh.dto.termination.TerminationFilterParams;
import com.promaty.rrhh.dto.termination.TerminationListDto;
import com.promaty.rrhh.dto.termination.UpdateTerminationDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.LegalTerminationCause;
import com.promaty.rrhh.entity.NonRehireReason;
import com.promaty.rrhh.entity.PlatformStatus;
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
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.shared.ProjectAccessSpecification;
import com.promaty.rrhh.services.shared.RequestApprovalActionsResolver;
import com.promaty.rrhh.services.termination.business.builder.TerminationQueryBuilder;
import com.promaty.rrhh.services.termination.business.mapper.TerminationMapper;
import com.promaty.rrhh.services.termination.business.validation.TerminationAnnulmentValidation;
import com.promaty.rrhh.services.termination.business.validation.TerminationValidation;

@Service
public class TerminationServiceImpl implements TerminationService {

	private static final String NOT_FOUND_MESSAGE = "Finiquito no encontrado.";
	private static final String PERMISSION_APPROVE = "termination.approve";
	private static final String PERMISSION_VALIDATE = "termination.validate";
	private static final String PERMISSION_UPDATE = "termination.update";
	private static final String PERMISSION_ANNUL = "termination.annul";
	private static final List<String> PENDING_CODES = List.of("PENDING_APPROVAL", "PENDING_VALIDATION");
	private static final String CODE_APPROVED = "APPROVED";
	private static final String SUBMODULE_TERMINATION = "termination";
	private static final String SUBMODULE_CONTRACT = "contract";
	private static final String SUBMODULE_STAFF = "staff";
	private static final String SUBMODULE_REQUEST = "request";
	private static final String CODE_ANNULLED = "ANNULLED";
	private static final String CODE_CONTRACT_ACTIVE = "ACTIVE";
	private static final String CODE_STAFF_ACTIVE = "ACTIVE";
	private static final String CODE_SYNC_ERROR = "SYNC_ERROR";

	private final TerminationRepository terminationRepository;
	private final RequestRepository requestRepository;
	private final ContractRepository contractRepository;
	private final StaffRepository staffRepository;
	private final PlatformStatusRepository platformStatusRepository;
	private final LegalTerminationCauseRepository legalTerminationCauseRepository;
	private final NonRehireReasonRepository nonRehireReasonRepository;
	private final TerminationValidation terminationValidation;
	private final TerminationAnnulmentValidation terminationAnnulmentValidation;
	private final RequestApprovalActionsResolver actionsResolver;
	private final RexPlusClient rexPlusClient;

	public TerminationServiceImpl(
		TerminationRepository terminationRepository,
		RequestRepository requestRepository,
		ContractRepository contractRepository,
		StaffRepository staffRepository,
		PlatformStatusRepository platformStatusRepository,
		LegalTerminationCauseRepository legalTerminationCauseRepository,
		NonRehireReasonRepository nonRehireReasonRepository,
		TerminationValidation terminationValidation,
		TerminationAnnulmentValidation terminationAnnulmentValidation,
		RequestApprovalActionsResolver actionsResolver,
		RexPlusClient rexPlusClient
	) {
		this.terminationRepository = terminationRepository;
		this.requestRepository = requestRepository;
		this.contractRepository = contractRepository;
		this.staffRepository = staffRepository;
		this.platformStatusRepository = platformStatusRepository;
		this.legalTerminationCauseRepository = legalTerminationCauseRepository;
		this.nonRehireReasonRepository = nonRehireReasonRepository;
		this.terminationValidation = terminationValidation;
		this.terminationAnnulmentValidation = terminationAnnulmentValidation;
		this.actionsResolver = actionsResolver;
		this.rexPlusClient = rexPlusClient;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<TerminationListDto> listTerminations(TerminationFilterParams filters, Pageable pageable) {
		Specification<Termination> specification = TerminationQueryBuilder.fromFilters(filters)
			.and(ProjectAccessSpecification.onProject());
		Page<TerminationListDto> page = terminationRepository.findAll(specification, pageable).map(TerminationMapper::toListDto);

		Map<Long, Request> requestsByTerminationId = findRequestsByTerminationIds(page.map(TerminationListDto::getId).getContent());
		return page.map(dto -> withRequestAndActions(dto, requestsByTerminationId.get(dto.getId())));
	}

	@Override
	@Transactional(readOnly = true)
	public TerminationDetailDto getTerminationDetail(Long id) {
		Termination termination = findById(id);
		TerminationDetailDto dto = TerminationMapper.toDetailDto(termination);
		Request request = requestRepository.findByEntityTypeAndEntityId(RequestEntityType.TERMINATION, id).orElse(null);
		dto.setRequestId(request == null ? null : request.getId());
		dto.setActions(resolveActions(request));
		return dto;
	}

	@Override
	@Transactional
	public void updateTermination(Long id, UpdateTerminationDto dto) {
		Termination existing = findById(id);
		terminationValidation.validateUpdate(existing, dto);

		existing.setTerminationDate(dto.getTerminationDate());
		existing.setLegalTerminationCause(resolveLegalCause(dto.getLegalTerminationCauseId()));
		existing.setDefinitiveTermination(dto.getDefinitiveTermination());
		existing.setNonRehireReason(dto.getNonRehireReasonId() == null ? null : resolveNonRehireReason(dto.getNonRehireReasonId()));
		existing.setNotes(dto.getNotes());
		terminationRepository.save(existing);
	}

	@Override
	@Transactional
	public void annulTermination(Long id, AnnulTerminationDto dto) {
		Termination termination = findById(id);
		terminationAnnulmentValidation.validateAnnul(termination);

		RexSyncResult syncResult = rexPlusClient.annulTermination(termination);
		if (!syncResult.success()) {
			termination.setStatus(resolveStatus(SUBMODULE_TERMINATION, CODE_SYNC_ERROR));
			terminationRepository.save(termination);
			throw new BusinessValidationException(
				"No se pudo sincronizar la anulación con Rex+.", Map.of("status", syncResult.errorMessage())
			);
		}

		revertContract(termination.getContract(), termination.getContractEndDateBeforeTermination());
		revertStaff(termination.getStaff());

		termination.setAnnulmentReason(dto.getReason());
		termination.setStatus(resolveStatus(SUBMODULE_TERMINATION, CODE_ANNULLED));
		terminationRepository.save(termination);

		requestRepository.findByEntityTypeAndEntityId(RequestEntityType.TERMINATION, id)
			.ifPresent(request -> {
				request.setStatus(resolveStatus(SUBMODULE_REQUEST, CODE_ANNULLED));
				requestRepository.save(request);
			});
	}

	private void revertContract(Contract contract, LocalDate endDateBeforeTermination) {
		contract.setEndDate(endDateBeforeTermination);
		contract.setStatus(resolveStatus(SUBMODULE_CONTRACT, CODE_CONTRACT_ACTIVE));
		contractRepository.save(contract);
	}

	private void revertStaff(Staff staff) {
		staff.setStatus(resolveStatus(SUBMODULE_STAFF, CODE_STAFF_ACTIVE));
		staff.setBlockReason(null);
		staffRepository.save(staff);
	}

	private PlatformStatus resolveStatus(String subModule, String code) {
		return platformStatusRepository.findBySubModuleAndCode(subModule, code)
			.orElseThrow(() -> new ResourceNotFoundException("El estado " + code + " de " + subModule + " no está sembrado."));
	}

	private LegalTerminationCause resolveLegalCause(Long id) {
		return legalTerminationCauseRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("La causal legal indicada no existe."));
	}

	private NonRehireReason resolveNonRehireReason(Long id) {
		return nonRehireReasonRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("El motivo de no recontratación indicado no existe."));
	}

	private Termination findById(Long id) {
		return terminationRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
	}

	private Map<Long, Request> findRequestsByTerminationIds(List<Long> terminationIds) {
		if (terminationIds.isEmpty()) {
			return Map.of();
		}
		return requestRepository.findByEntityTypeAndEntityIdIn(RequestEntityType.TERMINATION, terminationIds).stream()
			.collect(Collectors.toMap(Request::getEntityId, Function.identity()));
	}

	private TerminationListDto withRequestAndActions(TerminationListDto dto, Request request) {
		dto.setRequestId(request == null ? null : request.getId());
		dto.setActions(resolveActions(request));
		return dto;
	}

	private List<Action> resolveActions(Request request) {
		if (request == null) {
			return List.of();
		}
		String statusCode = request.getStatus().getCode();
		List<Action> actions = actionsResolver.resolve(statusCode, CurrentUserAuthorities.get(), PERMISSION_APPROVE, PERMISSION_VALIDATE);
		if (PENDING_CODES.contains(statusCode) && CurrentUserAuthorities.get().contains(PERMISSION_UPDATE)) {
			actions = new ArrayList<>(actions);
			actions.add(Action.UPDATE);
		}
		if (CODE_APPROVED.equals(statusCode) && CurrentUserAuthorities.get().contains(PERMISSION_ANNUL)) {
			actions = new ArrayList<>(actions);
			actions.add(Action.ANNUL);
		}
		return actions;
	}
}
