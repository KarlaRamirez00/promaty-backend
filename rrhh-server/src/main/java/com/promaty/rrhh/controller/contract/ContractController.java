package com.promaty.rrhh.controller.contract;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.contract.ContractDetailDto;
import com.promaty.rrhh.dto.contract.ContractFilterParams;
import com.promaty.rrhh.dto.contract.ContractListDto;
import com.promaty.rrhh.dto.file.FileAttachmentDto;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.entity.ContractAttachmentType;
import com.promaty.rrhh.services.contract.ContractService;

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

	@PostMapping(value = "/{id}/attachments", consumes = "multipart/form-data")
	@PreAuthorize("hasAuthority('contract.update')")
	public ResponseEntity<BaseData<FileAttachmentDto>> uploadAttachment(
		@PathVariable Long id,
		@RequestParam("type") ContractAttachmentType type,
		@RequestPart("file") MultipartFile file
	) {
		FileAttachmentDto result = contractService.uploadAttachment(id, type, file);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(result));
	}

	@DeleteMapping("/{id}/attachments/{attachmentId}")
	@PreAuthorize("hasAuthority('contract.update')")
	public ResponseEntity<BaseData<Void>> deleteAttachment(
		@PathVariable Long id,
		@PathVariable Long attachmentId
	) {
		contractService.deleteAttachment(id, attachmentId);
		return ResponseEntity.ok(BaseData.success(null));
	}
}
