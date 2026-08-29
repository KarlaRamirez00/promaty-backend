package com.promaty.user.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.contracts.auth.AuthValidationRequestDto;
import com.promaty.contracts.auth.AuthValidationResponseDto;
import com.promaty.user.dto.response.BaseData;
import com.promaty.user.services.auth.AuthService;

import jakarta.validation.Valid;

// Solo para llamadas internas de authorizer-server (ver docs/rbac.md); cuando exista el
// Gateway, no debe quedar expuesto al publico.
@RestController
@RequestMapping("/internal/auth")
public class AuthInternalController {

	private final AuthService authService;

	public AuthInternalController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/validate")
	public ResponseEntity<BaseData<AuthValidationResponseDto>> validate(@Valid @RequestBody AuthValidationRequestDto dto) {
		return ResponseEntity.ok(BaseData.success(authService.validate(dto)));
	}
}
