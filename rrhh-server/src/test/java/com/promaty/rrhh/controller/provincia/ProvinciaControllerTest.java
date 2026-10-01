package com.promaty.rrhh.controller.provincia;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.provincia.ProvinciaService;
import com.promaty.rrhh.support.TestJwt;

@WebMvcTest(ProvinciaController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class ProvinciaControllerTest {

	private static final String TOKEN = TestJwt.bearer("staff.read");
	private static final String SIN_PERMISOS = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProvinciaService provinciaService;

	@Test
	void list_conRegionId_retorna200ConOpciones() throws Exception {
		when(provinciaService.listOptionsByRegion(eq(8L))).thenReturn(List.of(
			new CatalogOptionDto(1L, "Santiago", "rm01")
		));

		mockMvc.perform(get("/provincias").param("regionId", "8").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].code").value("rm01"))
			.andExpect(jsonPath("$.meta.pagination").doesNotExist());
	}

	@Test
	void list_sinRegionId_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(get("/provincias").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.regionId").exists());
	}

	@Test
	void list_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/provincias").param("regionId", "8").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}
}
