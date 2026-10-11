package com.promaty.rrhh.services.contract;

import java.time.LocalDate;
import java.util.List;

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
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.services.contract.business.builder.ContractQueryBuilder;
import com.promaty.rrhh.services.contract.business.mapper.ContractMapper;
import com.promaty.rrhh.services.shared.ProjectAccessSpecification;

@Service
public class ContractServiceImpl implements ContractService {

	private static final String NO_ENCONTRADO = "Contrato no encontrado.";
	private static final String CODE_ACTIVE = "ACTIVE";
	private static final String CODE_EXPIRED = "EXPIRED";
	// Próximos N días para "por vencer" — fijo por ahora, ver rrhh-domain.md §8 para la idea de
	// hacerlo configurable en el panel de parámetros del sistema (no construido todavía).
	private static final int DIAS_POR_VENCER = 7;

	private final ContractRepository contractRepository;

	public ContractServiceImpl(ContractRepository contractRepository) {
		this.contractRepository = contractRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ContractListDto> listContracts(ContractFilterParams filters, Pageable pageable) {
		Specification<Contract> especificacion = ContractQueryBuilder.fromFilters(filters)
			.and(ProjectAccessSpecification.onProject());
		return contractRepository.findAll(especificacion, pageable)
			.map(ContractMapper::toListDto)
			.map(dto -> conAcciones(dto));
	}

	@Override
	@Transactional(readOnly = true)
	public ContractDetailDto getContractDetail(Long id) {
		Contract contract = buscarPorId(id);
		ContractDetailDto dto = ContractMapper.toDetailDto(contract);
		dto.setActions(List.of());
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

	private ContractListDto conAcciones(ContractListDto dto) {
		dto.setActions(List.of());
		return dto;
	}
}
