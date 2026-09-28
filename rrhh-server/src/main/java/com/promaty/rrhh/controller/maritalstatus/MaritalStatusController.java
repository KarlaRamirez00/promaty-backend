package com.promaty.rrhh.controller.maritalstatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.maritalstatus.MaritalStatusService;

@RestController
@RequestMapping("/maritalStatuses")
public class MaritalStatusController {

	private final MaritalStatusService maritalStatusService;

	public MaritalStatusController(MaritalStatusService maritalStatusService) {
		this.maritalStatusService = maritalStatusService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('staff.read')")
	public ResponseEntity<BaseListData<CatalogOptionDto>> list() {
		return ResponseEntity.ok(BaseListData.of(maritalStatusService.listOptions()));
	}
}
