package com.promaty.user.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDto {

	@NotBlank(message = "El nombre es obligatorio.")
	private String firstName;

	@NotBlank(message = "El apellido es obligatorio.")
	private String lastName;

	@NotBlank(message = "El email es obligatorio.")
	@Email(message = "El email no tiene un formato valido.")
	private String email;

	@NotBlank(message = "La contrasena es obligatoria.")
	@Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres.")
	private String password;

	private String phoneNumber;

	@NotNull(message = "El rol es obligatorio.")
	private Long roleId;
}
