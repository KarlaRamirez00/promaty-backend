package com.promaty.rrhh.controller.termination;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
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
}
