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
@Table(name = "project_specialty")
public class ProjectSpecialty extends BaseDatedEntity {

	@Column(nullable = false, unique = true, length = 150)
	private String name;

	@Column(nullable = false)
	private Boolean active = true;

	public void toggleActive() {
		this.active = !this.active;
	}
}
