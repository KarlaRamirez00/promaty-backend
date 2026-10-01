package com.promaty.rrhh.controller.comuna;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.comuna.ComunaService;

@RestController
@RequestMapping("/comunas")
public class ComunaController {

	private final ComunaService comunaService;

	public ComunaController(ComunaService comunaService) {
		this.comunaService = comunaService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('staff.read')")
	public ResponseEntity<BaseListData<CatalogOptionDto>> list(@RequestParam Long provinciaId) {
		return ResponseEntity.ok(BaseListData.of(comunaService.listOptionsByProvincia(provinciaId)));
	}
}
