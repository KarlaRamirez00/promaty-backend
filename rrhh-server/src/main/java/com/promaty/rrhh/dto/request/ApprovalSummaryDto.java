package com.promaty.rrhh.dto.request;

import java.time.LocalDateTime;

import com.promaty.rrhh.entity.ApprovalDecision;
import com.promaty.rrhh.entity.ApprovalLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalSummaryDto {

	private Long id;
	private ApprovalLevel level;
	private ApprovalDecision decision;
	private Long rejectionReasonId;
	private String rejectionReasonName;
	private String comment;
	private Long approverUserId;
	private LocalDateTime createdAt;
}
