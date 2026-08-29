package com.promaty.authorizer.services.auth;

import com.promaty.authorizer.dto.auth.LoginRequestDto;
import com.promaty.authorizer.dto.auth.LoginResponseDto;

public interface AuthService {

	LoginResponseDto login(LoginRequestDto dto);
}
