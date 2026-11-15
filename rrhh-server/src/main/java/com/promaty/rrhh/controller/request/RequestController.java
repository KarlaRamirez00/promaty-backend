package com.promaty.rrhh.controller.request;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.request.CreatedRequestDto;
import com.promaty.rrhh.dto.request.DecideRequestDto;
import com.promaty.rrhh.dto.request.RequestCountersDto;
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

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAnyAuthority('contract.create', 'termination.create', 'annex.create')")
	public ResponseEntity<BaseData<CreatedRequestDto>> create(
		@RequestPart("data") @Valid CreateRequestDto dto,
		@RequestPart(value = "files", required = false) List<MultipartFile> files
	) {
		CreatedRequestDto created = requestService.createRequest(dto, files);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(created));
	}

	@PatchMapping("/{id}/decide")
	@PreAuthorize("hasAnyAuthority('contract.approve', 'contract.validate', 'termination.approve', 'termination.validate', 'annex.approve', 'annex.validate')")
	public ResponseEntity<BaseData<Void>> decide(@PathVariable Long id, @Valid @RequestBody DecideRequestDto dto) {
		requestService.decideRequest(id, dto);
		return ResponseEntity.ok(BaseData.success(null));
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('contract.read', 'termination.read', 'annex.read')")
	public ResponseEntity<BaseListData<RequestListDto>> list(
		@ModelAttribute RequestFilterParams filters,
		@PageableDefault(sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
	) {
		RequestCountersDto counters = requestService.getCounters(filters.getEntityType());
		return ResponseEntity.ok(BaseListData.of(requestService.listRequests(filters, pageable), counters));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('contract.read', 'termination.read', 'annex.read')")
	public ResponseEntity<BaseData<RequestDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(requestService.getRequestDetail(id)));
	}
}
