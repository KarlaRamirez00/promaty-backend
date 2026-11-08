package com.promaty.contracts.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthValidationRequestDto {

	@NotBlank(message = "El email es obligatorio.")
	@Size(max = 254, message = "El email no puede superar los 254 caracteres.")
	private String email;

	@NotBlank(message = "La contrasena es obligatoria.")
	private String password;
}
