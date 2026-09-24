package com.promaty.rrhh.dto.colaborador;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateColaboradorDto {

	@NotBlank(message = "El nombre es obligatorio.")
	@Pattern(regexp = "^[A-Za-zÀ-ÿ ]+$", message = "El nombre solo puede contener letras.")
	@Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
	private String firstName;

	@NotBlank(message = "El apellido paterno es obligatorio.")
	@Pattern(regexp = "^[A-Za-zÀ-ÿ ]+$", message = "El apellido paterno solo puede contener letras.")
	@Size(max = 100, message = "El apellido paterno no puede superar los 100 caracteres.")
	private String paternalLastName;

	@NotBlank(message = "El apellido materno es obligatorio.")
	@Pattern(regexp = "^[A-Za-zÀ-ÿ ]+$", message = "El apellido materno solo puede contener letras.")
	@Size(max = 100, message = "El apellido materno no puede superar los 100 caracteres.")
	private String maternalLastName;

	@NotNull(message = "La fecha de nacimiento es obligatoria.")
	private LocalDate birthDate;

	@NotBlank(message = "El correo personal es obligatorio.")
	@Email(message = "El correo personal no tiene un formato valido.")
	private String personalEmail;

	@NotBlank(message = "El telefono es obligatorio.")
	@Pattern(regexp = "^9\\d{8}$", message = "El telefono debe tener el formato 9XXXXXXXX (sin +56).")
	private String phone1;
}
