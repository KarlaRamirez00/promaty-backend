package com.promaty.rrhh.entity;

import lombok.Getter;

@Getter
public enum SettingGroup {
	CONTRACTS("Contratos");

	private final String label;

	SettingGroup(String label) {
		this.label = label;
	}
}
