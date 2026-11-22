package com.promaty.rrhh.services.contract;

import java.time.LocalDate;
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

import com.promaty.rrhh.dto.contract.AnnulContractDto;
import com.promaty.rrhh.dto.contract.ContractCountersDto;
import com.promaty.rrhh.dto.contract.ContractDetailDto;
import com.promaty.rrhh.dto.contract.ContractFilterParams;
import com.promaty.rrhh.dto.contract.ContractListDto;
import com.promaty.rrhh.dto.contract.UpdateContractDto;
import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.AttachmentEntityType;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.entity.SettingKey;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.services.contract.business.ContractAttachmentRules;
import com.promaty.rrhh.services.contract.business.builder.AnnulContractBuilder;
import com.promaty.rrhh.services.contract.business.builder.ContractExpirationSpecification;
import com.promaty.rrhh.services.contract.business.builder.ContractQueryBuilder;
import com.promaty.rrhh.services.contract.business.builder.ContractSortField;
import com.promaty.rrhh.services.contract.business.builder.UpdateContractBuilder;
import com.promaty.rrhh.services.contract.business.validation.ContractAnnulmentValidation;
import com.promaty.rrhh.services.contract.business.validation.ContractValidation;
import com.promaty.rrhh.services.contract.business.mapper.ContractMapper;
import com.promaty.rrhh.services.rexplus.RexPlusClient;
import com.promaty.rrhh.services.rexplus.RexSyncResult;
import com.promaty.rrhh.services.setting.SettingProvider;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.shared.ProjectAccessSpecification;
import com.promaty.rrhh.services.shared.RequestApprovalActionsResolver;
import com.promaty.rrhh.services.shared.RequestEditPolicy;
import com.promaty.rrhh.services.shared.attachment.AttachmentOwner;
import com.promaty.rrhh.services.shared.attachment.AttachmentService;
import com.promaty.rrhh.services.shared.sort.ListSorting;
import com.promaty.rrhh.services.shared.sort.SortedQuery;

@Service
public class ContractServiceImpl implements ContractService {

	private static final String NO_ENCONTRADO = "Contrato no encontrado.";
	private static final String CODE_EXPIRED = "EXPIRED";
	private static final String PERMISO_APROBAR = "contract.approve";
	private static final String PERMISO_VALIDAR = "contract.validate";
	private static final String PERMISO_ACTUALIZAR = "contract.update";
	private static final String PERMISO_ANULAR = "contract.annul";
	private static final String SUBMODULE_CONTRACT = "contract";
	private static final String SYNC_FAILURE_MESSAGE = "No se pudo sincronizar la anulación con Rex+.";
	private static final String FIELD_STATUS = "status";

	private final ContractRepository contractRepository;
	private final RequestRepository requestRepository;
	private final RequestApprovalActionsResolver actionsResolver;
	private final SettingProvider settingProvider;
	private final AttachmentService attachmentService;
	private final RequestEditPolicy requestEditPolicy;
	private final ContractValidation contractValidation;
	private final UpdateContractBuilder updateContractBuilder;
	private final ContractAnnulmentValidation contractAnnulmentValidation;
	private final AnnulContractBuilder annulContractBuilder;
	private final RexPlusClient rexPlusClient;

	public ContractServiceImpl(
		ContractRepository contractRepository,
		RequestRepository requestRepository,
		RequestApprovalActionsResolver actionsResolver,
		SettingProvider settingProvider,
		AttachmentService attachmentService,
		RequestEditPolicy requestEditPolicy,
		ContractValidation contractValidation,
		UpdateContractBuilder updateContractBuilder,
		ContractAnnulmentValidation contractAnnulmentValidation,
		AnnulContractBuilder annulContractBuilder,
		RexPlusClient rexPlusClient
	) {
		this.contractRepository = contractRepository;
		this.requestRepository = requestRepository;
		this.actionsResolver = actionsResolver;
		this.settingProvider = settingProvider;
		this.attachmentService = attachmentService;
		this.requestEditPolicy = requestEditPolicy;
		this.contractValidation = contractValidation;
		this.updateContractBuilder = updateContractBuilder;
		this.contractAnnulmentValidation = contractAnnulmentValidation;
		this.annulContractBuilder = annulContractBuilder;
		this.rexPlusClient = rexPlusClient;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ContractListDto> listContracts(ContractFilterParams filters, Pageable pageable) {
		Specification<Contract> especificacion = withExpiringSoonFilter(
			ContractQueryBuilder.fromFilters(filters).and(ProjectAccessSpecification.onProject()),
			filters
		);
		SortedQuery<Contract> sorted = ListSorting.apply(especificacion, pageable, ContractSortField.class);
		Page<ContractListDto> pagina = contractRepository.findAll(sorted.specification(), sorted.pageable()).map(ContractMapper::toListDto);
		Map<Long, Request> requestsPorContrato = buscarRequestsPorContrato(pagina.map(ContractListDto::getId).getContent());
		Set<String> autoridades = CurrentUserAuthorities.get();
		return pagina.map(dto -> conRequestYAcciones(dto, requestsPorContrato.get(dto.getId()), autoridades));
	}

	@Override
	@Transactional(readOnly = true)
	public ContractDetailDto getContractDetail(Long id) {
		Contract contract = buscarPorId(id);
		ContractDetailDto dto = ContractMapper.toDetailDto(contract);
		Request request = requestRepository.findByEntityTypeAndEntityId(RequestEntityType.CONTRACT, id).orElse(null);
		dto.setRequestId(request == null ? null : request.getId());
		dto.setActions(resolveActions(request, contract.getStatus().getCode(), CurrentUserAuthorities.get()));
		AttachmentOwner owner = attachmentOwner(id);
		dto.setAttachments(attachmentService.list(owner));
		dto.setAttachmentRequirements(attachmentService.requirements(owner, ContractAttachmentRules.rules()));
		return dto;
	}

	@Override
	@Transactional
	public void updateContract(Long contractId, UpdateContractDto dto, List<MultipartFile> files) {
		Contract contract = buscarPorId(contractId);
		Request request = requestEditPolicy.requireEditable(RequestEntityType.CONTRACT, contractId);
		contractValidation.validateUpdate(contract, dto);
		updateContractBuilder.apply(contract, dto);
		attachmentService.sync(attachmentOwner(contractId), ContractAttachmentRules.rules(), dto.getAttachments(), files);
		request.setProject(contract.getProject());
		requestEditPolicy.replacePendingData(request, ContractMapper.toPendingData(contract));
		if (requestEditPolicy.returnToPendingApproval(request)) {
			contract.setStatus(requestEditPolicy.pendingApprovalStatus(SUBMODULE_CONTRACT));
		}
		contractRepository.save(contract);
	}

	@Override
	@Transactional
	public void annulContract(Long contractId, AnnulContractDto dto) {
		Contract contract = buscarPorId(contractId);
		contractAnnulmentValidation.validateAnnul(contract);

		RexSyncResult syncResult = rexPlusClient.annulContract(contract, dto.getReason());
		if (!syncResult.success()) {
			throw new BusinessValidationException(SYNC_FAILURE_MESSAGE, Map.of(FIELD_STATUS, syncResult.errorMessage()));
		}
		annulContractBuilder.apply(contract, dto.getReason());
	}

	@Override
	@Transactional(readOnly = true)
	public ContractCountersDto getCounters() {
		int expiringSoonDays = settingProvider.getInt(SettingKey.EXPIRING_CONTRACT_DAYS);
		return new ContractCountersDto(contarVencidos(), contarPorVencer(expiringSoonDays), expiringSoonDays);
	}

	private long contarVencidos() {
		Specification<Contract> especificacion = ProjectAccessSpecification.<Contract>onProject()
			.and((root, query, cb) -> cb.equal(root.get("status").get("code"), CODE_EXPIRED));
		return contractRepository.count(especificacion);
	}

	private long contarPorVencer(int expiringSoonDays) {
		Specification<Contract> especificacion = ProjectAccessSpecification.<Contract>onProject()
			.and(ContractExpirationSpecification.expiringSoon(LocalDate.now(), expiringSoonDays));
		return contractRepository.count(especificacion);
	}

	private Specification<Contract> withExpiringSoonFilter(Specification<Contract> base, ContractFilterParams filters) {
		if (!Boolean.TRUE.equals(filters.getExpiringSoon())) {
			return base;
		}
		int expiringSoonDays = settingProvider.getInt(SettingKey.EXPIRING_CONTRACT_DAYS);
		return base.and(ContractExpirationSpecification.expiringSoon(LocalDate.now(), expiringSoonDays));
	}

	private Contract buscarPorId(Long id) {
		return contractRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}

	private AttachmentOwner attachmentOwner(Long contractId) {
		return new AttachmentOwner(AttachmentEntityType.CONTRACT, contractId);
	}

	private Map<Long, Request> buscarRequestsPorContrato(List<Long> contractIds) {
		if (contractIds.isEmpty()) {
			return Map.of();
		}
		return requestRepository.findByEntityTypeAndEntityIdIn(RequestEntityType.CONTRACT, contractIds).stream()
			.collect(Collectors.toMap(Request::getEntityId, Function.identity()));
	}

	private ContractListDto conRequestYAcciones(ContractListDto dto, Request request, Set<String> autoridades) {
		dto.setRequestId(request == null ? null : request.getId());
		dto.setActions(resolveActions(request, dto.getStatusCode(), autoridades));
		return dto;
	}

	private List<Action> resolveActions(Request request, String contractStatusCode, Set<String> authorities) {
		String statusCode = request == null ? null : request.getStatus().getCode();
		List<Action> actions = new ArrayList<>(actionsResolver.resolve(statusCode, authorities, PERMISO_APROBAR, PERMISO_VALIDAR));
		if (requestEditPolicy.isEditable(request) && authorities.contains(PERMISO_ACTUALIZAR)) {
			actions.add(Action.UPDATE);
		}
		if (Contract.STATUS_CODE_ACTIVE.equals(contractStatusCode) && authorities.contains(PERMISO_ANULAR)) {
			actions.add(Action.ANNUL);
		}
		return actions;
	}
}
