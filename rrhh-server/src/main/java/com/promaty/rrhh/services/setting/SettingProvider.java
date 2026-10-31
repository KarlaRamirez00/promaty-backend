package com.promaty.rrhh.services.setting;

import java.math.BigDecimal;

import com.promaty.rrhh.entity.SettingKey;

public interface SettingProvider {

	int getInt(SettingKey key);

	BigDecimal getDecimal(SettingKey key);

	boolean getBoolean(SettingKey key);
}
