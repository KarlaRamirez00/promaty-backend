package com.promaty.rrhh.controller.contracttype;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.contracttype.ContractTypeService;

@RestController
@RequestMapping("/contractTypes")
public class ContractTypeController {

	private final ContractTypeService contractTypeService;

	public ContractTypeController(ContractTypeService contractTypeService) {
		this.contractTypeService = contractTypeService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('contract.read')")
	public ResponseEntity<BaseListData<CatalogOptionDto>> list() {
		return ResponseEntity.ok(BaseListData.of(contractTypeService.listOptions()));
	}
}
