package com.promaty.rrhh.services.contract;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.promaty.rrhh.dto.contract.ContractCountersDto;
import com.promaty.rrhh.dto.contract.ContractDetailDto;
import com.promaty.rrhh.dto.contract.ContractFilterParams;
import com.promaty.rrhh.dto.contract.ContractListDto;

public interface ContractService {

	Page<ContractListDto> listContracts(ContractFilterParams filters, Pageable pageable);

	ContractDetailDto getContractDetail(Long id);

	ContractCountersDto getCounters();
}
