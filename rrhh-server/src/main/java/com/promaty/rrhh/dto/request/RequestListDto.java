package com.promaty.rrhh.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.RequestAction;
import com.promaty.rrhh.entity.RequestEntityType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestListDto {

	private Long id;
	private RequestEntityType entityType;
	private RequestAction action;
	private Long entityId;
	private String projectName;
	private Long requesterUserId;
	private String statusName;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;
	private List<Action> actions;
}
