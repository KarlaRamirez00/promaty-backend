package com.promaty.rrhh.controller.nonrehirereason;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonDetailDto;
import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonListDto;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.nonrehirereason.NonRehireReasonService;
import com.promaty.rrhh.support.TestJwt;

@WebMvcTest(NonRehireReasonController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class NonRehireReasonControllerTest {

	private static final String TOKEN =
		TestJwt.bearer("nonRehireReason.read", "nonRehireReason.create", "nonRehireReason.update", "nonRehireReason.active");
	private static final String SIN_PERMISOS = TestJwt.bearer();

	private static final String CREATE_JSON = "{\"name\":\"Bajo desempeño\",\"code\":\"LOW_PERFORMANCE\"}";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NonRehireReasonService nonRehireReasonService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(nonRehireReasonService.createNonRehireReason(any())).thenReturn(10L);

		mockMvc.perform(post("/nonRehireReasons")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(CREATE_JSON))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10));
	}

	@Test
	void create_conNombreEnBlanco_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(post("/nonRehireReasons")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\" \",\"code\":\"LOW_PERFORMANCE\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.name").exists());
	}

	@Test
	void create_conCodigoEnBlanco_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(post("/nonRehireReasons")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Bajo desempeño\",\"code\":\" \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.code").exists());
	}

	@Test
	void list_retorna200ConData() throws Exception {
		when(nonRehireReasonService.listNonRehireReasons(any(), any())).thenReturn(new PageImpl<>(List.of(
			new NonRehireReasonListDto(1L, "Bajo desempeño", "LOW_PERFORMANCE", null, true, null, null, "system", null, List.of())
		)));

		mockMvc.perform(get("/nonRehireReasons").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].code").value("LOW_PERFORMANCE"));
	}

	@Test
	void detail_registroExiste_retorna200() throws Exception {
		when(nonRehireReasonService.getNonRehireReasonDetail(1L))
			.thenReturn(new NonRehireReasonDetailDto(1L, "Bajo desempeño", "LOW_PERFORMANCE", null, true, null, null, "system", null, List.of()));

		mockMvc.perform(get("/nonRehireReasons/1").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.code").value("LOW_PERFORMANCE"));
	}

	@Test
	void detail_registroNoExiste_retorna404() throws Exception {
		when(nonRehireReasonService.getNonRehireReasonDetail(99L))
			.thenThrow(new ResourceNotFoundException("Motivo de no recontratación no encontrado."));

		mockMvc.perform(get("/nonRehireReasons/99").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isNotFound());
	}

	@Test
	void update_conDatosValidos_retorna200() throws Exception {
		when(nonRehireReasonService.getNonRehireReasonDetail(1L))
			.thenReturn(new NonRehireReasonDetailDto(1L, "Bajo desempeño", "LOW_PERFORMANCE", null, true, null, null, "system", null, List.of()));

		mockMvc.perform(put("/nonRehireReasons/1")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(CREATE_JSON))
			.andExpect(status().isOk());
	}

	@Test
	void toggleActive_retorna200ConFlagAlternado() throws Exception {
		when(nonRehireReasonService.toggleNonRehireReasonActive(1L))
			.thenReturn(new NonRehireReasonDetailDto(1L, "Bajo desempeño", "LOW_PERFORMANCE", null, false, null, null, "system", null, List.of()));

		mockMvc.perform(patch("/nonRehireReasons/1/active").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.active").value(false));
	}

	@Test
	void list_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/nonRehireReasons").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}

	@Test
	void create_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(post("/nonRehireReasons")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content(CREATE_JSON))
			.andExpect(status().isForbidden());
	}
}
