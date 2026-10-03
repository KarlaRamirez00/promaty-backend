package com.promaty.rrhh.controller.site;

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
import com.promaty.rrhh.services.site.SiteService;
import com.promaty.rrhh.support.TestJwt;

@WebMvcTest(SiteController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class SiteControllerTest {

	private static final String TOKEN = TestJwt.bearer("contract.read");
	private static final String SIN_PERMISOS = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SiteService siteService;

	@Test
	void list_retorna200ConOpcionesYSinPaginacion() throws Exception {
		when(siteService.listOptions()).thenReturn(List.of(
			new CatalogOptionDto(1L, "Casa matriz", "MATRIZ")
		));

		mockMvc.perform(get("/sites").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].code").value("MATRIZ"))
			.andExpect(jsonPath("$.meta.pagination").doesNotExist());
	}

	@Test
	void list_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/sites").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}
}
