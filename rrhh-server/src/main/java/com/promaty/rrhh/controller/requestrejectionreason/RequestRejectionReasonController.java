package com.promaty.rrhh.controller.requestrejectionreason;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.requestrejectionreason.CreateRequestRejectionReasonDto;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonDetailDto;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonFilterParams;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonListDto;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonOptionDto;
import com.promaty.rrhh.dto.requestrejectionreason.UpdateRequestRejectionReasonDto;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.services.requestrejectionreason.RequestRejectionReasonService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/requestRejectionReasons")
public class RequestRejectionReasonController {

	private final RequestRejectionReasonService requestRejectionReasonService;

	public RequestRejectionReasonController(RequestRejectionReasonService requestRejectionReasonService) {
		this.requestRejectionReasonService = requestRejectionReasonService;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('requestRejectionReason.create')")
	public ResponseEntity<BaseData<Long>> create(@Valid @RequestBody CreateRequestRejectionReasonDto dto) {
		Long id = requestRejectionReasonService.createRequestRejectionReason(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(id));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('requestRejectionReason.read')")
	public ResponseEntity<BaseListData<RequestRejectionReasonListDto>> list(
		@ModelAttribute RequestRejectionReasonFilterParams filters,
		@PageableDefault(sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(requestRejectionReasonService.listRequestRejectionReasons(filters, pageable)));
	}

	@GetMapping("/selector")
	@PreAuthorize("hasAuthority('requestRejectionReason.read')")
	public ResponseEntity<BaseListData<RequestRejectionReasonOptionDto>> selector(@RequestParam String subModule) {
		return ResponseEntity.ok(BaseListData.of(requestRejectionReasonService.listOptionsBySubModule(subModule)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('requestRejectionReason.read')")
	public ResponseEntity<BaseData<RequestRejectionReasonDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(requestRejectionReasonService.getRequestRejectionReasonDetail(id)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('requestRejectionReason.update')")
	public ResponseEntity<BaseData<RequestRejectionReasonDetailDto>> update(
		@PathVariable Long id,
		@Valid @RequestBody UpdateRequestRejectionReasonDto dto
	) {
		requestRejectionReasonService.updateRequestRejectionReason(id, dto);
		return ResponseEntity.ok(BaseData.success(requestRejectionReasonService.getRequestRejectionReasonDetail(id)));
	}

	@PatchMapping("/{id}/active")
	@PreAuthorize("hasAuthority('requestRejectionReason.active')")
	public ResponseEntity<BaseData<RequestRejectionReasonDetailDto>> toggleActive(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(requestRejectionReasonService.toggleRequestRejectionReasonActive(id)));
	}
}
