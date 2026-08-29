package com.promaty.authorizer.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {

	@NotBlank(message = "El email es obligatorio.")
	private String email;

	@NotBlank(message = "La contrasena es obligatoria.")
	private String password;
}
