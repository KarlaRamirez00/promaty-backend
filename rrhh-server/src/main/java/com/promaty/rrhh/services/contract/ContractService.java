package com.promaty.rrhh.services.contract;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.contract.ContractCountersDto;
import com.promaty.rrhh.dto.contract.ContractDetailDto;
import com.promaty.rrhh.dto.contract.ContractFilterParams;
import com.promaty.rrhh.dto.contract.ContractListDto;
import com.promaty.rrhh.dto.contract.UpdateContractAttachmentsDto;
import com.promaty.rrhh.dto.contract.UpdateContractDto;

public interface ContractService {

	Page<ContractListDto> listContracts(ContractFilterParams filters, Pageable pageable);

	ContractDetailDto getContractDetail(Long id);

	ContractCountersDto getCounters();

	void updateContract(Long contractId, UpdateContractDto dto, List<MultipartFile> files);

	void updateAttachments(Long contractId, UpdateContractAttachmentsDto dto, List<MultipartFile> files);
}
