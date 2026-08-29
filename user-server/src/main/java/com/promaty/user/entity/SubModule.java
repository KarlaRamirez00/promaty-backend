package com.promaty.user.entity;

import com.promaty.user.entity.base.BaseDatedEntity;

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
@Table(name = "sub_modules")
public class SubModule extends BaseDatedEntity {

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false)
	private String alias;

	@Column
	private String description;

	@Column(nullable = false)
	private String path;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "module_id", nullable = false)
	private Module module;
}
