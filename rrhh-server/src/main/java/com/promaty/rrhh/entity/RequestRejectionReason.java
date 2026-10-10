package com.promaty.rrhh.entity;

import com.promaty.rrhh.entity.base.BaseDatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "request_rejection_reason")
public class RequestRejectionReason extends BaseDatedEntity {

	@Column(nullable = false, unique = true, length = 150)
	private String name;

	@Column(length = 250)
	private String description;

	// user-server vive en otra BD (database-per-service): sin FK real, solo el nombre del submodulo.
	@Column(name = "sub_module", nullable = false)
	private String subModule;

	@Column(nullable = false)
	private Boolean active = true;

	public void toggleActive() {
		this.active = !this.active;
	}
}
