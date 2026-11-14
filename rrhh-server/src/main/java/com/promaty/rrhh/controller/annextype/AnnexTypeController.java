package com.promaty.rrhh.controller.annextype;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.annextype.AnnexTypeService;

@RestController
@RequestMapping("/annexTypes")
public class AnnexTypeController {

	private final AnnexTypeService annexTypeService;

	public AnnexTypeController(AnnexTypeService annexTypeService) {
		this.annexTypeService = annexTypeService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('annex.read')")
	public ResponseEntity<BaseListData<CatalogOptionDto>> list() {
		return ResponseEntity.ok(BaseListData.of(annexTypeService.listOptions()));
	}
}
