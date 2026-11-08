package com.promaty.user.services.auth;

import com.promaty.user.dto.auth.PasswordResetDto;

public interface PasswordResetService {

	void resetPassword(PasswordResetDto dto);
}
