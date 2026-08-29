package com.promaty.authorizer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.promaty.authorizer.dto.response.BaseData;
import com.promaty.contracts.auth.AuthValidationRequestDto;
import com.promaty.contracts.auth.AuthValidationResponseDto;

@FeignClient(name = "user-server")
public interface UserServerClient {

	@PostMapping("/internal/auth/validate")
	BaseData<AuthValidationResponseDto> validate(@RequestBody AuthValidationRequestDto dto);
}
