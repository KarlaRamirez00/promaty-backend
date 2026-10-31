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
@Table(name = "system_setting")
public class SystemSetting extends BaseDatedEntity {

	@Column(nullable = false, unique = true, length = 100)
	private String settingKey;

	@Column(nullable = false)
	private String settingValue;

	public static SystemSetting of(SettingKey key) {
		SystemSetting setting = new SystemSetting();
		setting.setSettingKey(key.name());
		return setting;
	}
}
