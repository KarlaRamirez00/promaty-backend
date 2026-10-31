package com.promaty.rrhh.services.setting.business.validation;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.setting.UpdateSettingDto;
import com.promaty.rrhh.entity.SettingKey;
import com.promaty.rrhh.exception.BusinessValidationException;

@Component
public class SettingValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";

	public void validateUpdate(SettingKey key, UpdateSettingDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		key.validationError(dto.getValue()).ifPresent(error -> errores.put("value", error));

		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
