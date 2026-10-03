package com.promaty.rrhh.services.contracttype;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.ContractTypeRepository;

@Service
public class ContractTypeServiceImpl implements ContractTypeService {

	private final ContractTypeRepository contractTypeRepository;

	public ContractTypeServiceImpl(ContractTypeRepository contractTypeRepository) {
		this.contractTypeRepository = contractTypeRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return contractTypeRepository.findByActiveTrueOrderByName()
			.stream()
			.map(contractType -> new CatalogOptionDto(contractType.getId(), contractType.getName(), contractType.getCode()))
			.toList();
	}
}
