package com.promaty.rrhh.controller.setting;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.setting.SettingDto;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.setting.SettingService;
import com.promaty.rrhh.support.TestJwt;

// @WebMvcTest en Boot 4.1 no auto-configura Spring Security; @EnableWebSecurity restituye el
// bean HttpSecurity que SecurityConfig necesita.
@WebMvcTest(SettingController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class SettingControllerTest {

	private static final String TOKEN = TestJwt.bearer("setting.read", "setting.update");
	private static final String SOLO_LECTURA = TestJwt.bearer("setting.read");
	private static final String SIN_PERMISOS = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SettingService settingService;

	@Test
	void list_conPermiso_retorna200ConLasConfiguraciones() throws Exception {
		when(settingService.listSettings()).thenReturn(List.of(settingDto("7")));

		mockMvc.perform(get("/settings").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].key").value("EXPIRING_CONTRACT_DAYS"))
			.andExpect(jsonPath("$.data[0].value").value("7"));
	}

	@Test
	void list_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/settings").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}

	@Test
	void update_conPermisoYValorValido_retorna200ConLaConfiguracionActualizada() throws Exception {
		when(settingService.updateSetting(eq("EXPIRING_CONTRACT_DAYS"), any())).thenReturn(settingDto("30"));

		mockMvc.perform(put("/settings/EXPIRING_CONTRACT_DAYS")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"value\":\"30\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.value").value("30"))
			.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void update_conValorEnBlanco_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(put("/settings/EXPIRING_CONTRACT_DAYS")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"value\":\" \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.value").exists());
	}

	@Test
	void update_conClaveInexistente_retorna404() throws Exception {
		when(settingService.updateSetting(eq("NO_EXISTE"), any()))
			.thenThrow(new ResourceNotFoundException("Configuración no encontrada."));

		mockMvc.perform(put("/settings/NO_EXISTE")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"value\":\"5\"}"))
			.andExpect(status().isNotFound());
	}

	@Test
	void update_soloConPermisoDeLectura_retorna403() throws Exception {
		mockMvc.perform(put("/settings/EXPIRING_CONTRACT_DAYS")
				.header(HttpHeaders.AUTHORIZATION, SOLO_LECTURA)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"value\":\"30\"}"))
			.andExpect(status().isForbidden());
	}

	private SettingDto settingDto(String value) {
		return new SettingDto("EXPIRING_CONTRACT_DAYS", "CONTRACTS", "Contratos",
			"Días para considerar un contrato por vencer", "INTEGER", value, "7",
			new BigDecimal("1"), new BigDecimal("365"), null, null);
	}
}
