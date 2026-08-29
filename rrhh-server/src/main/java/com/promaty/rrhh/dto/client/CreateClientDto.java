package com.promaty.rrhh.dto.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateClientDto {

	@NotBlank(message = "El nombre es obligatorio.")
	private String name;
}
