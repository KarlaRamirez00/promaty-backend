package com.promaty.authorizer.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.authorizer.dto.auth.LoginRequestDto;
import com.promaty.authorizer.dto.auth.LoginResponseDto;
import com.promaty.authorizer.dto.response.BaseData;
import com.promaty.authorizer.services.auth.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<BaseData<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto dto) {
		return ResponseEntity.ok(BaseData.success(authService.login(dto)));
	}
}
