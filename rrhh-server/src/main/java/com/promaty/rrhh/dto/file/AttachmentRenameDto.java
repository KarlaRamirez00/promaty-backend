package com.promaty.rrhh.dto.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachmentRenameDto {

	private Long id;

	@NotBlank(message = "El nombre del archivo es obligatorio.")
	@Size(max = 255, message = "El nombre del archivo no puede superar los 255 caracteres.")
	private String name;
}
