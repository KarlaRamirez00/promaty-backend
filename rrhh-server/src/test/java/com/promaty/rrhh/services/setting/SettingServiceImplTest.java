package com.promaty.rrhh.services.setting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.setting.SettingDto;
import com.promaty.rrhh.dto.setting.UpdateSettingDto;
import com.promaty.rrhh.entity.SettingKey;
import com.promaty.rrhh.entity.SystemSetting;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.SystemSettingRepository;
import com.promaty.rrhh.services.setting.business.validation.SettingValidation;

@ExtendWith(MockitoExtension.class)
class SettingServiceImplTest {

	private static final String KEY = "EXPIRING_CONTRACT_DAYS";

	@Mock
	private SystemSettingRepository settingRepository;
	@Spy
	private SettingValidation settingValidation = new SettingValidation();

	@InjectMocks
	private SettingServiceImpl service;

	@Test
	void listSettings_sinFilaGuardada_retornaElValorPorDefecto() {
		when(settingRepository.findAll()).thenReturn(List.of());

		List<SettingDto> settings = service.listSettings();

		assertThat(settings).hasSize(SettingKey.values().length);
		assertThat(settings.get(0).getKey()).isEqualTo(KEY);
		assertThat(settings.get(0).getValue()).isEqualTo("7");
		assertThat(settings.get(0).getUpdatedBy()).isNull();
	}

	@Test
	void listSettings_conFilaGuardada_retornaElValorGuardado() {
		SystemSetting stored = SystemSetting.of(SettingKey.EXPIRING_CONTRACT_DAYS);
		stored.setSettingValue("30");
		when(settingRepository.findAll()).thenReturn(List.of(stored));

		SettingDto dto = service.listSettings().get(0);

		assertThat(dto.getValue()).isEqualTo("30");
		assertThat(dto.getDefaultValue()).isEqualTo("7");
	}

	@Test
	void updateSetting_claveInexistente_lanzaResourceNotFoundException() {
		assertThatThrownBy(() -> service.updateSetting("NO_EXISTE", dto("5")))
			.isInstanceOf(ResourceNotFoundException.class);
		verify(settingRepository, never()).save(any());
	}

	@Test
	void updateSetting_valorFueraDeRango_lanzaBusinessValidationExceptionSinGuardar() {
		assertThatThrownBy(() -> service.updateSetting(KEY, dto("999")))
			.isInstanceOf(BusinessValidationException.class);
		verify(settingRepository, never()).save(any());
	}

	@Test
	void updateSetting_sinFilaPrevia_creaLaFilaConElValorNormalizado() {
		when(settingRepository.findBySettingKey(KEY)).thenReturn(Optional.empty());
		when(settingRepository.save(any(SystemSetting.class))).thenAnswer(invocation -> invocation.getArgument(0));

		SettingDto result = service.updateSetting(KEY, dto(" 15.0 "));

		ArgumentCaptor<SystemSetting> captor = ArgumentCaptor.forClass(SystemSetting.class);
		verify(settingRepository).save(captor.capture());
		assertThat(captor.getValue().getSettingKey()).isEqualTo(KEY);
		assertThat(captor.getValue().getSettingValue()).isEqualTo("15");
		assertThat(result.getValue()).isEqualTo("15");
	}

	@Test
	void updateSetting_conFilaPrevia_actualizaLaMismaFila() {
		SystemSetting existing = SystemSetting.of(SettingKey.EXPIRING_CONTRACT_DAYS);
		existing.setSettingValue("7");
		when(settingRepository.findBySettingKey(KEY)).thenReturn(Optional.of(existing));
		when(settingRepository.save(existing)).thenReturn(existing);

		service.updateSetting(KEY, dto("21"));

		assertThat(existing.getSettingValue()).isEqualTo("21");
	}

	private UpdateSettingDto dto(String value) {
		UpdateSettingDto dto = new UpdateSettingDto();
		dto.setValue(value);
		return dto;
	}
}
