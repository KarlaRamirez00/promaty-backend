package com.promaty.rrhh.controller.termination;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.file.FileAttachmentDto;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.termination.AnnulTerminationDto;
import com.promaty.rrhh.dto.termination.TerminationDetailDto;
import com.promaty.rrhh.dto.termination.TerminationFilterParams;
import com.promaty.rrhh.dto.termination.TerminationListDto;
import com.promaty.rrhh.dto.termination.UpdateTerminationDto;
import com.promaty.rrhh.services.termination.TerminationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/terminations")
public class TerminationController {

	private final TerminationService terminationService;

	public TerminationController(TerminationService terminationService) {
		this.terminationService = terminationService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('termination.read')")
	public ResponseEntity<BaseListData<TerminationListDto>> list(
		@ModelAttribute TerminationFilterParams filters,
		@PageableDefault(sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(terminationService.listTerminations(filters, pageable)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('termination.read')")
	public ResponseEntity<BaseData<TerminationDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(terminationService.getTerminationDetail(id)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('termination.update')")
	public ResponseEntity<BaseData<TerminationDetailDto>> update(
		@PathVariable Long id,
		@Valid @RequestBody UpdateTerminationDto dto
	) {
		terminationService.updateTermination(id, dto);
		return ResponseEntity.ok(BaseData.success(terminationService.getTerminationDetail(id)));
	}

	@PatchMapping("/{id}/annul")
	@PreAuthorize("hasAuthority('termination.annul')")
	public ResponseEntity<BaseData<TerminationDetailDto>> annul(
		@PathVariable Long id,
		@Valid @RequestBody AnnulTerminationDto dto
	) {
		terminationService.annulTermination(id, dto);
		return ResponseEntity.ok(BaseData.success(terminationService.getTerminationDetail(id)));
	}

	@PostMapping(value = "/{id}/attachments", consumes = "multipart/form-data")
	@PreAuthorize("hasAuthority('termination.update')")
	public ResponseEntity<BaseData<FileAttachmentDto>> uploadAttachment(
		@PathVariable Long id,
		@RequestPart("file") MultipartFile file
	) {
		FileAttachmentDto result = terminationService.uploadAttachment(id, file);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(result));
	}

	@DeleteMapping("/{id}/attachments/{attachmentId}")
	@PreAuthorize("hasAuthority('termination.update')")
	public ResponseEntity<BaseData<Void>> deleteAttachment(
		@PathVariable Long id,
		@PathVariable Long attachmentId
	) {
		terminationService.deleteAttachment(id, attachmentId);
		return ResponseEntity.ok(BaseData.success(null));
	}
}
