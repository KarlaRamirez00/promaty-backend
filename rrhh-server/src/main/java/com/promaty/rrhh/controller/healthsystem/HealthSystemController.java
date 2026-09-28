package com.promaty.rrhh.controller.healthsystem;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.healthsystem.HealthSystemService;

@RestController
@RequestMapping("/healthSystems")
public class HealthSystemController {

	private final HealthSystemService healthSystemService;

	public HealthSystemController(HealthSystemService healthSystemService) {
		this.healthSystemService = healthSystemService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('staff.read')")
	public ResponseEntity<BaseListData<CatalogOptionDto>> list() {
		return ResponseEntity.ok(BaseListData.of(healthSystemService.listOptions()));
	}
}
