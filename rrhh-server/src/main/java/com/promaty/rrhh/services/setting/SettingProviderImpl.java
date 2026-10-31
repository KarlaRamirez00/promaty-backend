package com.promaty.rrhh.services.setting;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.entity.SettingKey;
import com.promaty.rrhh.entity.SystemSetting;
import com.promaty.rrhh.repository.SystemSettingRepository;

@Service
public class SettingProviderImpl implements SettingProvider {

	private final SystemSettingRepository settingRepository;

	public SettingProviderImpl(SystemSettingRepository settingRepository) {
		this.settingRepository = settingRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public int getInt(SettingKey key) {
		return Integer.parseInt(rawValue(key));
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal getDecimal(SettingKey key) {
		return new BigDecimal(rawValue(key));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean getBoolean(SettingKey key) {
		return Boolean.parseBoolean(rawValue(key));
	}

	private String rawValue(SettingKey key) {
		return settingRepository.findBySettingKey(key.name())
			.map(SystemSetting::getSettingValue)
			.orElse(key.getDefaultValue());
	}
}
