package com.promaty.rrhh.services.setting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.setting.SettingDto;
import com.promaty.rrhh.dto.setting.UpdateSettingDto;
import com.promaty.rrhh.entity.SettingKey;
import com.promaty.rrhh.entity.SystemSetting;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.SystemSettingRepository;
import com.promaty.rrhh.services.setting.business.mapper.SettingMapper;
import com.promaty.rrhh.services.setting.business.validation.SettingValidation;

@Service
public class SettingServiceImpl implements SettingService {

	private static final String NO_ENCONTRADO = "Configuración no encontrada.";

	private final SystemSettingRepository settingRepository;
	private final SettingValidation settingValidation;

	public SettingServiceImpl(SystemSettingRepository settingRepository, SettingValidation settingValidation) {
		this.settingRepository = settingRepository;
		this.settingValidation = settingValidation;
	}

	@Override
	@Transactional(readOnly = true)
	public List<SettingDto> listSettings() {
		Map<String, SystemSetting> storedByKey = settingRepository.findAll().stream()
			.collect(Collectors.toMap(SystemSetting::getSettingKey, Function.identity()));
		return Arrays.stream(SettingKey.values())
			.map(key -> SettingMapper.toDto(key, storedByKey.get(key.name())))
			.toList();
	}

	@Override
	@Transactional
	public SettingDto updateSetting(String keyName, UpdateSettingDto dto) {
		SettingKey key = SettingKey.fromName(keyName)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
		settingValidation.validateUpdate(key, dto);
		SystemSetting setting = settingRepository.findBySettingKey(key.name())
			.orElseGet(() -> SystemSetting.of(key));
		setting.setSettingValue(key.normalize(dto.getValue()));
		return SettingMapper.toDto(key, settingRepository.save(setting));
	}
}
