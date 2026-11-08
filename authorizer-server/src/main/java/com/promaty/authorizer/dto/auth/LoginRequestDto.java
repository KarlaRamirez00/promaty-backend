package com.promaty.authorizer.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {

	@NotBlank(message = "El email es obligatorio.")
	@Size(max = 254, message = "El email no puede superar los 254 caracteres.")
	private String email;

	@NotBlank(message = "La contrasena es obligatoria.")
	private String password;
}
