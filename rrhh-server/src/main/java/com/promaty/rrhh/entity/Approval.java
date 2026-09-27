package com.promaty.rrhh.entity;

import com.promaty.rrhh.entity.base.BaseDatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "approvals")
public class Approval extends BaseDatedEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "request_id", nullable = false)
	private Request request;

	// user-server vive en otra BD (database-per-service): sin FK real, solo el id.
	@Column(name = "approver_user_id", nullable = false)
	private Long approverUserId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ApprovalLevel level;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ApprovalDecision decision;

	@Column(name = "rejection_reason")
	private String rejectionReason;

	@Column
	private String comment;
}
