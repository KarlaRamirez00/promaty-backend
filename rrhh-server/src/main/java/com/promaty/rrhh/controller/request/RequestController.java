package com.promaty.rrhh.controller.request;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.response.BaseData;
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
}
