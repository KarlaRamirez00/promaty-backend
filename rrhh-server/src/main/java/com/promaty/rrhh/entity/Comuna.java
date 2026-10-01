package com.promaty.rrhh.entity;

import com.promaty.rrhh.entity.base.BaseDatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "comuna")
public class Comuna extends BaseDatedEntity {

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, unique = true, length = 10)
	private String code;

	@Column(nullable = false)
	private Boolean active = true;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "provincia_id", nullable = false)
	private Provincia provincia;
}
