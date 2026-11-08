package com.promaty.user.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.user.dto.auth.PasswordRecoveryDto;
import com.promaty.user.dto.auth.PasswordResetDto;
import com.promaty.user.dto.response.BaseData;
import com.promaty.user.services.auth.PasswordRecoveryService;
import com.promaty.user.services.auth.PasswordResetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final PasswordResetService passwordResetService;
	private final PasswordRecoveryService passwordRecoveryService;

	public AuthController(PasswordResetService passwordResetService, PasswordRecoveryService passwordRecoveryService) {
		this.passwordResetService = passwordResetService;
		this.passwordRecoveryService = passwordRecoveryService;
	}

	@PostMapping("/password-recovery")
	public ResponseEntity<BaseData<Void>> requestPasswordRecovery(@Valid @RequestBody PasswordRecoveryDto dto) {
		passwordRecoveryService.requestRecovery(dto.getEmail());
		return ResponseEntity.ok(BaseData.success(null));
	}

	@PostMapping("/password-reset")
	public ResponseEntity<BaseData<Void>> resetPassword(@Valid @RequestBody PasswordResetDto dto) {
		passwordResetService.resetPassword(dto);
		return ResponseEntity.ok(BaseData.success(null));
	}
}
