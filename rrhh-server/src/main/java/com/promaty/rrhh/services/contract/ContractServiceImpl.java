package com.promaty.rrhh.services.contract;

import java.time.LocalDate;
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

import com.promaty.rrhh.dto.contract.ContractCountersDto;
import com.promaty.rrhh.dto.contract.ContractDetailDto;
import com.promaty.rrhh.dto.contract.ContractFilterParams;
import com.promaty.rrhh.dto.contract.ContractListDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.services.contract.business.builder.ContractQueryBuilder;
import com.promaty.rrhh.services.contract.business.mapper.ContractMapper;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.shared.ProjectAccessSpecification;
import com.promaty.rrhh.services.shared.RequestApprovalActionsResolver;

@Service
public class ContractServiceImpl implements ContractService {

	private static final String NO_ENCONTRADO = "Contrato no encontrado.";
	private static final String CODE_ACTIVE = "ACTIVE";
	private static final String CODE_EXPIRED = "EXPIRED";
	private static final String PERMISO_APROBAR = "contract.approve";
	private static final String PERMISO_VALIDAR = "contract.validate";
	// Próximos N días para "por vencer" — fijo por ahora, ver rrhh-domain.md §8 para la idea de
	// hacerlo configurable en el panel de parámetros del sistema (no construido todavía).
	private static final int DIAS_POR_VENCER = 7;

	private final ContractRepository contractRepository;
	private final RequestRepository requestRepository;
	private final RequestApprovalActionsResolver actionsResolver;

	public ContractServiceImpl(
		ContractRepository contractRepository,
		RequestRepository requestRepository,
		RequestApprovalActionsResolver actionsResolver
	) {
		this.contractRepository = contractRepository;
		this.requestRepository = requestRepository;
		this.actionsResolver = actionsResolver;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ContractListDto> listContracts(ContractFilterParams filters, Pageable pageable) {
		Specification<Contract> especificacion = ContractQueryBuilder.fromFilters(filters)
			.and(ProjectAccessSpecification.onProject());
		Page<ContractListDto> pagina = contractRepository.findAll(especificacion, pageable).map(ContractMapper::toListDto);
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
		String statusCode = request == null ? null : request.getStatus().getCode();
		dto.setActions(actionsResolver.resolve(statusCode, CurrentUserAuthorities.get(), PERMISO_APROBAR, PERMISO_VALIDAR));
		return dto;
	}

	@Override
	@Transactional(readOnly = true)
	public ContractCountersDto getCounters() {
		return new ContractCountersDto(contarVencidos(), contarPorVencer());
	}

	private long contarVencidos() {
		Specification<Contract> especificacion = ProjectAccessSpecification.<Contract>onProject()
			.and((root, query, cb) -> cb.equal(root.get("status").get("code"), CODE_EXPIRED));
		return contractRepository.count(especificacion);
	}

	private long contarPorVencer() {
		LocalDate hoy = LocalDate.now();
		LocalDate limite = hoy.plusDays(DIAS_POR_VENCER);
		Specification<Contract> especificacion = ProjectAccessSpecification.<Contract>onProject()
			.and((root, query, cb) -> cb.equal(root.get("status").get("code"), CODE_ACTIVE))
			.and((root, query, cb) -> cb.between(root.get("endDate"), hoy, limite));
		return contractRepository.count(especificacion);
	}

	private Contract buscarPorId(Long id) {
		return contractRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
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
		String statusCode = request == null ? null : request.getStatus().getCode();
		dto.setActions(actionsResolver.resolve(statusCode, autoridades, PERMISO_APROBAR, PERMISO_VALIDAR));
		return dto;
	}
}
