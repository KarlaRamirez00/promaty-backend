package com.promaty.rrhh.dto.request;

import com.promaty.rrhh.entity.ApprovalDecision;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DecideRequestDto {

	@NotNull(message = "La decision es obligatoria.")
	private ApprovalDecision decision;

	private Long rejectionReasonId;

	private String comment;
}
