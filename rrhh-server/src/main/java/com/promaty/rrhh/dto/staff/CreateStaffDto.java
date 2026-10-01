package com.promaty.rrhh.dto.staff;

import java.time.LocalDate;

import com.promaty.rrhh.entity.AccountType;
import com.promaty.rrhh.entity.ClothingSize;
import com.promaty.rrhh.entity.IdentificationType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStaffDto {

	@NotNull(message = "El tipo de identificacion es obligatorio.")
	private IdentificationType identificationType;

	@NotBlank(message = "El numero de identificacion es obligatorio.")
	@Size(max = 20, message = "El numero de identificacion no puede superar los 20 caracteres.")
	private String identificationNumber;

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

	@NotNull(message = "El sexo registral es obligatorio.")
	private Long registeredSexId;

	@NotNull(message = "El estado civil es obligatorio.")
	private Long maritalStatusId;

	@NotNull(message = "La nacionalidad es obligatoria.")
	private Long nationalityId;

	@NotBlank(message = "El telefono es obligatorio.")
	@Pattern(regexp = "^9\\d{8}$", message = "El telefono debe tener el formato 9XXXXXXXX (sin +56).")
	private String phone1;

	@NotBlank(message = "El telefono de emergencia es obligatorio.")
	@Pattern(regexp = "^9\\d{8}$", message = "El telefono de emergencia debe tener el formato 9XXXXXXXX (sin +56).")
	private String emergencyPhone;

	@NotBlank(message = "El nombre del contacto de emergencia es obligatorio.")
	@Size(max = 150, message = "El nombre del contacto de emergencia no puede superar los 150 caracteres.")
	private String emergencyContactName;

	@NotBlank(message = "El domicilio es obligatorio.")
	@Size(max = 200, message = "El domicilio no puede superar los 200 caracteres.")
	private String address;

	@NotNull(message = "La comuna es obligatoria.")
	private Long comunaId;

	@NotNull(message = "Debe indicar si tiene hijos.")
	private Boolean hasChildren;

	private Integer childrenCount;

	@NotBlank(message = "El correo personal es obligatorio.")
	@Email(message = "El correo personal no tiene un formato valido.")
	private String personalEmail;

	@NotNull(message = "El numero de calzado es obligatorio.")
	@Min(value = 35, message = "El numero de calzado debe estar entre 35 y 46.")
	@Max(value = 46, message = "El numero de calzado debe estar entre 35 y 46.")
	private Integer shoeSize;

	@NotNull(message = "La talla de ropa es obligatoria.")
	private ClothingSize clothingSize;

	@NotNull(message = "El nivel educacional es obligatorio.")
	private Long educationLevelId;

	@NotNull(message = "La AFP es obligatoria.")
	private Long afpId;

	@NotNull(message = "El sistema de salud es obligatorio.")
	private Long healthSystemId;

	@NotNull(message = "El banco es obligatorio.")
	private Long bankId;

	@NotNull(message = "El tipo de cuenta es obligatorio.")
	private AccountType accountType;

	@NotBlank(message = "El numero de cuenta es obligatorio.")
	@Size(max = 30, message = "El numero de cuenta no puede superar los 30 caracteres.")
	private String accountNumber;
}
