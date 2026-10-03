package com.promaty.rrhh.controller.site;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.site.SiteService;

@RestController
@RequestMapping("/sites")
public class SiteController {

	private final SiteService siteService;

	public SiteController(SiteService siteService) {
		this.siteService = siteService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('contract.read')")
	public ResponseEntity<BaseListData<CatalogOptionDto>> list() {
		return ResponseEntity.ok(BaseListData.of(siteService.listOptions()));
	}
}
