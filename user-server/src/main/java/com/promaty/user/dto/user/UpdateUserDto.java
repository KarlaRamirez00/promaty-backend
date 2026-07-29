package com.promaty.user.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {

	@NotNull(message = "El id es obligatorio.")
	private Long id;

	@NotBlank(message = "El nombre es obligatorio.")
	private String firstName;

	@NotBlank(message = "El apellido es obligatorio.")
	private String lastName;

	@NotBlank(message = "El email es obligatorio.")
	@Email(message = "El email no tiene un formato valido.")
	private String email;

	private String phoneNumber;

	@NotNull(message = "El rol es obligatorio.")
	private Long roleId;
}
