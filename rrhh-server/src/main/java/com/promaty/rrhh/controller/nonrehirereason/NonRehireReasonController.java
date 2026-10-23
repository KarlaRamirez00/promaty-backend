package com.promaty.rrhh.controller.nonrehirereason;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.nonrehirereason.CreateNonRehireReasonDto;
import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonDetailDto;
import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonFilterParams;
import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonListDto;
import com.promaty.rrhh.dto.nonrehirereason.UpdateNonRehireReasonDto;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.services.nonrehirereason.NonRehireReasonService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/nonRehireReasons")
public class NonRehireReasonController {

	private final NonRehireReasonService nonRehireReasonService;

	public NonRehireReasonController(NonRehireReasonService nonRehireReasonService) {
		this.nonRehireReasonService = nonRehireReasonService;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('nonRehireReason.create')")
	public ResponseEntity<BaseData<Long>> create(@Valid @RequestBody CreateNonRehireReasonDto dto) {
		Long id = nonRehireReasonService.createNonRehireReason(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(id));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('nonRehireReason.read')")
	public ResponseEntity<BaseListData<NonRehireReasonListDto>> list(
		@ModelAttribute NonRehireReasonFilterParams filters,
		@PageableDefault(sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(nonRehireReasonService.listNonRehireReasons(filters, pageable)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('nonRehireReason.read')")
	public ResponseEntity<BaseData<NonRehireReasonDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(nonRehireReasonService.getNonRehireReasonDetail(id)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('nonRehireReason.update')")
	public ResponseEntity<BaseData<NonRehireReasonDetailDto>> update(
		@PathVariable Long id,
		@Valid @RequestBody UpdateNonRehireReasonDto dto
	) {
		nonRehireReasonService.updateNonRehireReason(id, dto);
		return ResponseEntity.ok(BaseData.success(nonRehireReasonService.getNonRehireReasonDetail(id)));
	}

	@PatchMapping("/{id}/active")
	@PreAuthorize("hasAuthority('nonRehireReason.active')")
	public ResponseEntity<BaseData<NonRehireReasonDetailDto>> toggleActive(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(nonRehireReasonService.toggleNonRehireReasonActive(id)));
	}
}
