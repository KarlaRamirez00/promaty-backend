package com.promaty.rrhh.controller.colaborador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.colaborador.ColaboradorDetailDto;
import com.promaty.rrhh.dto.colaborador.ColaboradorListDto;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.colaborador.ColaboradorService;
import com.promaty.rrhh.support.TestJwt;

@WebMvcTest(ColaboradorController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class ColaboradorControllerTest {

	private static final String TOKEN =
		TestJwt.bearer("colaborador.read", "colaborador.create", "colaborador.update", "colaborador.active");
	private static final String SIN_PERMISOS = TestJwt.bearer();

	private static final String COLABORADOR_JSON = """
		{
		  "identificationType": "RUT",
		  "identificationNumber": "12345678-5",
		  "firstName": "Juan",
		  "paternalLastName": "Perez",
		  "maternalLastName": "Soto",
		  "birthDate": "1990-01-01",
		  "personalEmail": "juan.perez@example.com",
		  "phone1": "912345678"
		}
		""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ColaboradorService colaboradorService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(colaboradorService.createColaborador(any())).thenReturn(10L);

		mockMvc.perform(post("/colaboradores")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(COLABORADOR_JSON))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10));
	}

	@Test
	void create_conRutInvalido_retorna400() throws Exception {
		mockMvc.perform(post("/colaboradores")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(COLABORADOR_JSON.replace("\"phone1\": \"912345678\"", "\"phone1\": \"12345\"")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.phone1").exists());
	}

	@Test
	void list_retorna200ConData() throws Exception {
		Page<ColaboradorListDto> pagina = new PageImpl<>(List.of(
			new ColaboradorListDto(1L, IdentificationType.RUT, "12345678-5", "Juan", "Perez", "Soto", true,
				null, null, "system", null, List.of())
		));
		when(colaboradorService.listColaboradores(any(), any())).thenReturn(pagina);

		mockMvc.perform(get("/colaboradores").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].firstName").value("Juan"));
	}

	@Test
	void detail_registroExiste_retorna200() throws Exception {
		when(colaboradorService.getColaboradorDetail(1L)).thenReturn(detalle());

		mockMvc.perform(get("/colaboradores/1").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.identificationNumber").value("12345678-5"));
	}

	@Test
	void detail_registroNoExiste_retorna404() throws Exception {
		when(colaboradorService.getColaboradorDetail(99L))
			.thenThrow(new ResourceNotFoundException("Colaborador no encontrado."));

		mockMvc.perform(get("/colaboradores/99").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isNotFound());
	}

	@Test
	void update_conDatosValidos_retorna200() throws Exception {
		when(colaboradorService.getColaboradorDetail(1L)).thenReturn(detalle());

		mockMvc.perform(put("/colaboradores/1")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "firstName": "Juan",
					  "paternalLastName": "Perez",
					  "maternalLastName": "Soto",
					  "birthDate": "1990-01-01",
					  "personalEmail": "juan.perez@example.com",
					  "phone1": "912345678"
					}
					"""))
			.andExpect(status().isOk());
	}

	@Test
	void toggleActive_retorna200() throws Exception {
		when(colaboradorService.toggleColaboradorActive(1L)).thenReturn(detalle());

		mockMvc.perform(patch("/colaboradores/1/active").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk());
	}

	@Test
	void list_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/colaboradores").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}

	@Test
	void create_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(post("/colaboradores")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content(COLABORADOR_JSON))
			.andExpect(status().isForbidden());
	}

	private ColaboradorDetailDto detalle() {
		return new ColaboradorDetailDto(1L, IdentificationType.RUT, "12345678-5", "Juan", "Perez", "Soto",
			LocalDate.of(1990, 1, 1), "juan.perez@example.com", "912345678", true, null, null, "system", null,
			List.of());
	}
}
