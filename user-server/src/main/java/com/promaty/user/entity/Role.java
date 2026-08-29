package com.promaty.user.entity;

import java.util.HashSet;
import java.util.Set;

import com.promaty.user.entity.base.BaseDatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
	private Boolean active = true;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "role_permission",
		joinColumns = @JoinColumn(name = "role_id"),
		inverseJoinColumns = @JoinColumn(name = "permission_id")
	)
	private Set<Permission> permissions = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "role_submodule",
		joinColumns = @JoinColumn(name = "role_id"),
		inverseJoinColumns = @JoinColumn(name = "submodule_id")
	)
	private Set<SubModule> subModules = new HashSet<>();

	public void toggleActive() {
		this.active = !this.active;
	}
}
