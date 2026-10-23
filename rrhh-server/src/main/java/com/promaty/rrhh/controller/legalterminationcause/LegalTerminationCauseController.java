package com.promaty.rrhh.controller.legalterminationcause;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.legalterminationcause.LegalTerminationCauseService;

@RestController
@RequestMapping("/legalTerminationCauses")
public class LegalTerminationCauseController {

	private final LegalTerminationCauseService legalTerminationCauseService;

	public LegalTerminationCauseController(LegalTerminationCauseService legalTerminationCauseService) {
		this.legalTerminationCauseService = legalTerminationCauseService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('termination.read')")
	public ResponseEntity<BaseListData<CatalogOptionDto>> list() {
		return ResponseEntity.ok(BaseListData.of(legalTerminationCauseService.listOptions()));
	}
}
