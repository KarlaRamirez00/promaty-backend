package com.promaty.rrhh.services.setting;

import java.util.List;

import com.promaty.rrhh.dto.setting.SettingDto;
import com.promaty.rrhh.dto.setting.UpdateSettingDto;

public interface SettingService {

	List<SettingDto> listSettings();

	SettingDto updateSetting(String key, UpdateSettingDto dto);
}
