package com.promaty.rrhh.controller.bank;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.bank.BankOptionDto;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.services.bank.BankService;

@RestController
@RequestMapping("/banks")
public class BankController {

	private final BankService bankService;

	public BankController(BankService bankService) {
		this.bankService = bankService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('staff.read')")
	public ResponseEntity<BaseListData<BankOptionDto>> list() {
		return ResponseEntity.ok(BaseListData.of(bankService.listOptions()));
	}
}
