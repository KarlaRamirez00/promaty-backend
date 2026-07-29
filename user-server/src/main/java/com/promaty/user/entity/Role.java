package com.promaty.user.entity;

import com.promaty.user.entity.base.BaseDatedEntity;

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
@Table(name = "roles")
public class Role extends BaseDatedEntity {

	@Column(nullable = false, unique = true)
	private String name;

	@Column
	private String description;

	@Column(nullable = false)
	private Boolean status = true;
}
