package com.promaty.user.dto.auth;

import com.promaty.user.dto.shared.StrongPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetDto {

	@NotBlank(message = "El email es obligatorio.")
	@Email(message = "El email no tiene un formato valido.")
	private String email;

	@NotBlank(message = "El codigo temporal es obligatorio.")
	private String resetCode;

	@NotBlank(message = "La contrasena es obligatoria.")
	@StrongPassword
	private String password;
}
