package com.promaty.rrhh.controller.requestrejectionreason;

import static org.mockito.ArgumentMatchers.any;
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
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonOptionDto;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonSubModuleOptionDto;
import com.promaty.rrhh.services.requestrejectionreason.RequestRejectionReasonService;
import com.promaty.rrhh.support.TestJwt;

@WebMvcTest(RequestRejectionReasonController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class RequestRejectionReasonControllerTest {

	private static final String TOKEN_READ = TestJwt.bearer("requestRejectionReason.read");
	private static final String TOKEN_APPROVE = TestJwt.bearer("contract.approve");
	private static final String TOKEN_VALIDATE = TestJwt.bearer("contract.validate");
	private static final String SIN_PERMISOS = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RequestRejectionReasonService requestRejectionReasonService;

	@Test
	void selector_conPermisoDeAprobarContrato_retorna200() throws Exception {
		when(requestRejectionReasonService.listOptionsBySubModule("contracts"))
			.thenReturn(List.of(new RequestRejectionReasonOptionDto(1L, "Documentacion incompleta")));

		mockMvc.perform(get("/requestRejectionReasons/selector")
				.param("subModule", "contracts")
				.header(HttpHeaders.AUTHORIZATION, TOKEN_APPROVE))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].name").value("Documentacion incompleta"));
	}

	@Test
	void selector_conPermisoDeValidarContrato_retorna200() throws Exception {
		when(requestRejectionReasonService.listOptionsBySubModule(any())).thenReturn(List.of());

		mockMvc.perform(get("/requestRejectionReasons/selector")
				.param("subModule", "contracts")
				.header(HttpHeaders.AUTHORIZATION, TOKEN_VALIDATE))
			.andExpect(status().isOk());
	}

	@Test
	void selector_sinNingunPermisoRelevante_retorna403() throws Exception {
		mockMvc.perform(get("/requestRejectionReasons/selector")
				.param("subModule", "contracts")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}

	@Test
	void subModules_conPermisoDeLectura_retorna200ConElCatalogo() throws Exception {
		when(requestRejectionReasonService.listSubModules())
			.thenReturn(List.of(new RequestRejectionReasonSubModuleOptionDto("contracts", "Contratos")));

		mockMvc.perform(get("/requestRejectionReasons/subModules").header(HttpHeaders.AUTHORIZATION, TOKEN_READ))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].code").value("contracts"))
			.andExpect(jsonPath("$.data[0].name").value("Contratos"));
	}

	@Test
	void subModules_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/requestRejectionReasons/subModules").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}
}
