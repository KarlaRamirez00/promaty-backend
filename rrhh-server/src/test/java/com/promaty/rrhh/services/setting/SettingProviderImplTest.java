package com.promaty.rrhh.services.setting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.entity.SettingKey;
import com.promaty.rrhh.entity.SystemSetting;
import com.promaty.rrhh.repository.SystemSettingRepository;

@ExtendWith(MockitoExtension.class)
class SettingProviderImplTest {

	private static final String KEY = "EXPIRING_CONTRACT_DAYS";

	@Mock
	private SystemSettingRepository settingRepository;

	@InjectMocks
	private SettingProviderImpl provider;

	@Test
	void getInt_sinFilaGuardada_retornaElValorPorDefecto() {
		when(settingRepository.findBySettingKey(KEY)).thenReturn(Optional.empty());

		assertThat(provider.getInt(SettingKey.EXPIRING_CONTRACT_DAYS)).isEqualTo(7);
	}

	@Test
	void getInt_conFilaGuardada_retornaElValorGuardado() {
		SystemSetting stored = SystemSetting.of(SettingKey.EXPIRING_CONTRACT_DAYS);
		stored.setSettingValue("45");
		when(settingRepository.findBySettingKey(KEY)).thenReturn(Optional.of(stored));

		assertThat(provider.getInt(SettingKey.EXPIRING_CONTRACT_DAYS)).isEqualTo(45);
	}
}
