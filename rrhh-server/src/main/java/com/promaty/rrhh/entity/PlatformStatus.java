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
@Table(name = "platform_status")
public class PlatformStatus extends BaseDatedEntity {

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String code;

	@Column
	private String description;

	@Column(name = "sort_order")
	private Integer sortOrder;

	@Column(name = "sub_module", nullable = false)
	private String subModule;

	@Column(nullable = false)
	private Boolean active = true;
}
