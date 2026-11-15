package com.promaty.rrhh.dto.file;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentItemDto {

	private Long id;

	private Integer fileIndex;

	@Size(max = 255, message = "El nombre del archivo no puede superar los 255 caracteres.")
	private String name;

	private String type;
}
