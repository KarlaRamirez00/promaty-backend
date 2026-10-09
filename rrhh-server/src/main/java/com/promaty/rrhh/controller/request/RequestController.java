package com.promaty.rrhh.controller.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.request.DecideRequestDto;
import com.promaty.rrhh.dto.request.RequestDetailDto;
import com.promaty.rrhh.dto.request.RequestFilterParams;
import com.promaty.rrhh.dto.request.RequestListDto;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.services.request.RequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/requests")
public class RequestController {

	private final RequestService requestService;

	public RequestController(RequestService requestService) {
		this.requestService = requestService;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('contract.create')")
	public ResponseEntity<BaseData<Long>> create(@Valid @RequestBody CreateRequestDto dto) {
		Long id = requestService.createRequest(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(id));
	}

	@PatchMapping("/{id}/decide")
	@PreAuthorize("hasAnyAuthority('contract.approve', 'contract.validate')")
	public ResponseEntity<BaseData<Void>> decide(@PathVariable Long id, @Valid @RequestBody DecideRequestDto dto) {
		requestService.decideRequest(id, dto);
		return ResponseEntity.ok(BaseData.success(null));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('contract.read')")
	public ResponseEntity<BaseListData<RequestListDto>> list(
		@ModelAttribute RequestFilterParams filters,
		@PageableDefault(sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(requestService.listRequests(filters, pageable)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('contract.read')")
	public ResponseEntity<BaseData<RequestDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(requestService.getRequestDetail(id)));
	}
}
