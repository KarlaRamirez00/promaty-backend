package com.promaty.rrhh.services.setting.business.mapper;

import com.promaty.rrhh.dto.setting.SettingDto;
import com.promaty.rrhh.entity.SettingKey;
import com.promaty.rrhh.entity.SystemSetting;

public final class SettingMapper {

	private SettingMapper() {
	}

	public static SettingDto toDto(SettingKey key, SystemSetting stored) {
		boolean isStored = stored != null;
		return new SettingDto(
			key.name(),
			key.getGroup().name(),
			key.getGroup().getLabel(),
			key.getLabel(),
			key.getType().name(),
			isStored ? stored.getSettingValue() : key.getDefaultValue(),
			key.getDefaultValue(),
			key.getMin(),
			key.getMax(),
			isStored ? stored.getUpdatedAt() : null,
			isStored ? stored.getUpdatedBy() : null
		);
	}
}
