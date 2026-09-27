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
@Table(name = "requests")
public class Request extends BaseDatedEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "entity_type", nullable = false, length = 30)
	private RequestEntityType entityType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RequestAction action;

	@Column(name = "entity_id")
	private Long entityId;

	@Column(name = "pending_data", nullable = false, columnDefinition = "TEXT")
	private String pendingData;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	// user-server vive en otra BD (database-per-service): sin FK real, solo el id.
	@Column(name = "requester_user_id", nullable = false)
	private Long requesterUserId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "status_id", nullable = false)
	private PlatformStatus status;
}
