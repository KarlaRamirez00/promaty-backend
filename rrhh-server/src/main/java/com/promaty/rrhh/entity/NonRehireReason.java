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
@Table(name = "non_rehire_reason")
public class NonRehireReason extends BaseDatedEntity {

	@Column(nullable = false, unique = true, length = 150)
	private String name;

	@Column(nullable = false, unique = true, length = 40)
	private String code;

	@Column(length = 250)
	private String description;

	@Column(nullable = false)
	private Boolean active = true;

	public void toggleActive() {
		this.active = !this.active;
	}
}
