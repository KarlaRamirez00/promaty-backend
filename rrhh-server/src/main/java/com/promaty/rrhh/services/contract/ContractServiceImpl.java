package com.promaty.rrhh.services.contract;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.contract.ContractDetailDto;
import com.promaty.rrhh.dto.contract.ContractFilterParams;
import com.promaty.rrhh.dto.contract.ContractListDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.services.contract.business.builder.ContractQueryBuilder;
import com.promaty.rrhh.services.contract.business.mapper.ContractMapper;

@Service
public class ContractServiceImpl implements ContractService {

	private static final String NO_ENCONTRADO = "Contrato no encontrado.";

	private final ContractRepository contractRepository;

	public ContractServiceImpl(ContractRepository contractRepository) {
		this.contractRepository = contractRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ContractListDto> listContracts(ContractFilterParams filters, Pageable pageable) {
		Specification<Contract> especificacion = ContractQueryBuilder.fromFilters(filters);
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

	private Contract buscarPorId(Long id) {
		return contractRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}

	private ContractListDto conAcciones(ContractListDto dto) {
		dto.setActions(List.of());
		return dto;
	}
}
