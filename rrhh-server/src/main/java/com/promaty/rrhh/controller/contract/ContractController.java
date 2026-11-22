package com.promaty.rrhh.controller.contract;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.contract.AnnulContractDto;
import com.promaty.rrhh.dto.contract.ContractDetailDto;
import com.promaty.rrhh.dto.contract.ContractFilterParams;
import com.promaty.rrhh.dto.contract.ContractListDto;
import com.promaty.rrhh.dto.contract.UpdateContractDto;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.services.contract.ContractService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/contracts")
public class ContractController {

	private final ContractService contractService;

	public ContractController(ContractService contractService) {
		this.contractService = contractService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('contract.read')")
	public ResponseEntity<BaseListData<ContractListDto>> list(
		@ModelAttribute ContractFilterParams filters,
		@PageableDefault(sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(contractService.listContracts(filters, pageable), contractService.getCounters()));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('contract.read')")
	public ResponseEntity<BaseData<ContractDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(contractService.getContractDetail(id)));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAuthority('contract.update')")
	public ResponseEntity<BaseData<ContractDetailDto>> update(
		@PathVariable Long id,
		@RequestPart("data") @Valid UpdateContractDto dto,
		@RequestPart(value = "files", required = false) List<MultipartFile> files
	) {
		contractService.updateContract(id, dto, files);
		return ResponseEntity.ok(BaseData.success(contractService.getContractDetail(id)));
	}

	@PatchMapping("/{id}/annul")
	@PreAuthorize("hasAuthority('contract.annul')")
	public ResponseEntity<BaseData<ContractDetailDto>> annul(
		@PathVariable Long id,
		@Valid @RequestBody AnnulContractDto dto
	) {
		contractService.annulContract(id, dto);
		return ResponseEntity.ok(BaseData.success(contractService.getContractDetail(id)));
	}
}
