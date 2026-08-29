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
@Table(name = "modules")
public class Module extends BaseDatedEntity {

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false)
	private String alias;

	@Column
	private String description;
}
