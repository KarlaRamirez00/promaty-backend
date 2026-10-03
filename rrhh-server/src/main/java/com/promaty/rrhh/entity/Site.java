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
@Table(name = "site")
public class Site extends BaseDatedEntity {

	@Column(nullable = false, unique = true, length = 150)
	private String name;

	@Column(nullable = false, unique = true, length = 30)
	private String code;

	@Column(nullable = false)
	private Boolean active = true;
}
