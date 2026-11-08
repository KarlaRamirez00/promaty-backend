package com.promaty.user.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordRecoveryDto {

	@NotBlank(message = "El email es obligatorio.")
	@Email(message = "El email no tiene un formato valido.")
	private String email;
}
