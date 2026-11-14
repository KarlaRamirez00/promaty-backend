package com.promaty.rrhh.controller.annextype;

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
import com.promaty.rrhh.services.annextype.AnnexTypeService;
import com.promaty.rrhh.support.TestJwt;

@WebMvcTest(AnnexTypeController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class AnnexTypeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AnnexTypeService annexTypeService;

	@Test
	void list_conAnnexRead_retornaOpcionesConCodigoSinPaginacion() throws Exception {
		when(annexTypeService.listOptions()).thenReturn(List.of(
			new CatalogOptionDto(1L, "Prórroga de plazo", "CONTRACT_EXTENSION")
		));

		mockMvc.perform(get("/annexTypes").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("annex.read")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].code").value("CONTRACT_EXTENSION"))
			.andExpect(jsonPath("$.meta.pagination").doesNotExist());
	}

	@Test
	void list_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/annexTypes").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer()))
			.andExpect(status().isForbidden());
	}
}
