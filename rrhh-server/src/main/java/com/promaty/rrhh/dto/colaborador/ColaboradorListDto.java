package com.promaty.rrhh.dto.colaborador;

import java.time.LocalDateTime;
import java.util.List;

import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.IdentificationType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ColaboradorListDto {

	private Long id;
	private IdentificationType identificationType;
	private String identificationNumber;
	private String firstName;
	private String paternalLastName;
	private String maternalLastName;
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;
	private List<Action> actions;
}
