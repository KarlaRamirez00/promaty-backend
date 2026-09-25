package com.promaty.user.entity;

import com.promaty.user.entity.base.BaseDatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_project_access", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "project_id"}))
public class UserProjectAccess extends BaseDatedEntity {

	// user vive en la misma BD (user-server): FK real. project vive en rrhh-server
	// (database-per-service): solo el id, sin FK real entre bases de datos distintas.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "project_id", nullable = false)
	private Long projectId;
}
