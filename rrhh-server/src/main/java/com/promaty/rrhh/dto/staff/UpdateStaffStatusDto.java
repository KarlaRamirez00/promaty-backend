package com.promaty.rrhh.dto.staff;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStaffStatusDto {

	@NotNull(message = "El estado es obligatorio.")
	private Long statusId;

	private String blockReason;
}
