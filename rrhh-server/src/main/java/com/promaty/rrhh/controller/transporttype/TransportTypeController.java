package com.promaty.rrhh.controller.transporttype;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.transporttype.TransportTypeService;

@RestController
@RequestMapping("/transportTypes")
public class TransportTypeController {

	private final TransportTypeService transportTypeService;

	public TransportTypeController(TransportTypeService transportTypeService) {
		this.transportTypeService = transportTypeService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('contract.read')")
	public ResponseEntity<BaseListData<CatalogOptionDto>> list() {
		return ResponseEntity.ok(BaseListData.of(transportTypeService.listOptions()));
	}
}
