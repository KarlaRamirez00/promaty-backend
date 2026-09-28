package com.promaty.rrhh.controller.nationality;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.nationality.NationalityService;

@RestController
@RequestMapping("/nationalities")
public class NationalityController {

	private final NationalityService nationalityService;

	public NationalityController(NationalityService nationalityService) {
		this.nationalityService = nationalityService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('staff.read')")
	public ResponseEntity<BaseListData<CatalogOptionDto>> list() {
		return ResponseEntity.ok(BaseListData.of(nationalityService.listOptions()));
	}
}
