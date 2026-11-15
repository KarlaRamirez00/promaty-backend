package com.promaty.rrhh.controller.annex;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.annex.AnnexDetailDto;
import com.promaty.rrhh.dto.annex.AnnexFilterParams;
import com.promaty.rrhh.dto.annex.AnnexListDto;
import com.promaty.rrhh.dto.annex.UpdateAnnexDto;
import com.promaty.rrhh.services.annex.AnnexService;
import com.promaty.rrhh.support.TestJwt;

@WebMvcTest(AnnexController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class AnnexControllerTest {

	private static final String LECTURA = TestJwt.bearer("annex.read");
	private static final String EDICION = TestJwt.bearer("annex.update");
	private static final String SIN_PERMISOS = TestJwt.bearer();
	private static final String UPDATE_JSON = """
		{"effectiveDate":"2026-11-20","description":"Aumento acordado","newBaseSalary":950000}
		""";

	@Autowired
	private MockMvc mockMvc;

	private static MockMultipartHttpServletRequestBuilder multipartPut(String url, String json, String token) {
		MockMultipartHttpServletRequestBuilder builder = multipart(HttpMethod.PUT, url);
		builder.file(new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes(StandardCharsets.UTF_8)));
		builder.header(HttpHeaders.AUTHORIZATION, token);
		return builder;
	}

	@MockitoBean
	private AnnexService annexService;

	@Test
	void list_conAnnexRead_retorna200Paginado() throws Exception {
		AnnexListDto fila = new AnnexListDto();
		fila.setId(1L);
		when(annexService.listAnnexes(any(AnnexFilterParams.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(fila)));

		mockMvc.perform(get("/annexes").header(HttpHeaders.AUTHORIZATION, LECTURA))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].id").value(1))
			.andExpect(jsonPath("$.meta.pagination.total").value(1));
	}

	@Test
	void list_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/annexes").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}

	@Test
	void detail_conAnnexRead_retorna200() throws Exception {
		AnnexDetailDto detalle = new AnnexDetailDto();
		detalle.setId(1L);
		when(annexService.getAnnexDetail(1L)).thenReturn(detalle);

		mockMvc.perform(get("/annexes/1").header(HttpHeaders.AUTHORIZATION, LECTURA))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(1));
	}

	@Test
	void update_conDatosValidos_retorna200YDelegaEnElService() throws Exception {
		when(annexService.getAnnexDetail(1L)).thenReturn(new AnnexDetailDto());

		mockMvc.perform(multipartPut("/annexes/1", UPDATE_JSON, EDICION))
			.andExpect(status().isOk());

		verify(annexService).updateAnnex(eq(1L), any(UpdateAnnexDto.class), any());
	}

	@Test
	void update_sinDescripcion_retorna400ConElCampo() throws Exception {
		mockMvc.perform(multipartPut("/annexes/1", "{\"effectiveDate\":\"2026-11-20\"}", EDICION))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.description").exists());
	}

	@Test
	void update_soloConLectura_retorna403() throws Exception {
		mockMvc.perform(multipartPut("/annexes/1", UPDATE_JSON, LECTURA))
			.andExpect(status().isForbidden());
	}
}
