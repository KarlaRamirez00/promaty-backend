package com.promaty.rrhh.dto.request;

import java.time.LocalDate;

import com.promaty.rrhh.entity.RequestEntityType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestFilterParams {

	private RequestEntityType entityType;
	private Long projectId;
	private Long statusId;
	private Long requesterUserId;
	private String search;
	private LocalDate createdFrom;
	private LocalDate createdTo;
}
