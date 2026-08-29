package com.promaty.user.entity;

import com.promaty.user.entity.base.BaseDatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "project_id", nullable = false)
	private Long projectId;
}
