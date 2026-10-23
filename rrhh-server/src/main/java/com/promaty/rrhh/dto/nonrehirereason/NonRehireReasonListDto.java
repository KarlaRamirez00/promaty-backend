package com.promaty.rrhh.dto.nonrehirereason;

import java.time.LocalDateTime;
import java.util.List;

import com.promaty.rrhh.dto.shared.Action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NonRehireReasonListDto {

	private Long id;
	private String name;
	private String code;
	private String description;
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;
	private List<Action> actions;
}
