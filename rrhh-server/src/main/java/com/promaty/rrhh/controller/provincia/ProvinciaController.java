package com.promaty.rrhh.controller.provincia;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.provincia.ProvinciaService;

@RestController
@RequestMapping("/provincias")
public class ProvinciaController {

	private final ProvinciaService provinciaService;

	public ProvinciaController(ProvinciaService provinciaService) {
		this.provinciaService = provinciaService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('staff.read')")
	public ResponseEntity<BaseListData<CatalogOptionDto>> list(@RequestParam Long regionId) {
		return ResponseEntity.ok(BaseListData.of(provinciaService.listOptionsByRegion(regionId)));
	}
}
