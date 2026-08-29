package com.promaty.user.services.auth;

import com.promaty.contracts.auth.AuthValidationRequestDto;
import com.promaty.contracts.auth.AuthValidationResponseDto;

public interface AuthService {

	AuthValidationResponseDto validate(AuthValidationRequestDto dto);
}
