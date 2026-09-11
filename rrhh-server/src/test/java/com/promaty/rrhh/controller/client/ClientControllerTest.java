package com.promaty.rrhh.controller.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.client.ClientDetailDto;
import com.promaty.rrhh.dto.client.ClientListDto;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.client.ClientService;
import com.promaty.rrhh.support.TestJwt;

// @WebMvcTest en Boot 4.1 no auto-configura Spring Security; @EnableWebSecurity restituye el
// bean HttpSecurity que SecurityConfig necesita.
@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class ClientControllerTest {

	// Token con todos los permisos de client para los tests de flujo; la autorizacion por permiso se prueba aparte.
	private static final String TOKEN =
		TestJwt.bearer("client.read", "client.create", "client.update", "client.active");

	private static final String SIN_PERMISOS = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ClientService clientService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(clientService.createClient(any())).thenReturn(10L);

		mockMvc.perform(post("/clients")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Sodimac\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10))
			.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void create_conNombreEnBlanco_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(post("/clients")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\" \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.name").exists())
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void list_retorna200ConDataYPaginacion() throws Exception {
		Page<ClientListDto> pagina = new PageImpl<>(
			List.of(new ClientListDto(1L, "Sodimac", true, LocalDateTime.of(2026, 1, 15, 10, 0), null, List.of())),
			PageRequest.of(0, 20),
			1
		);
		when(clientService.listClients(any(), any())).thenReturn(pagina);

		mockMvc.perform(get("/clients").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].name").value("Sodimac"))
			.andExpect(jsonPath("$.data[0].createdAt").exists())
			.andExpect(jsonPath("$.meta.pagination.total").value(1));
	}

	@Test
	void detail_registroExiste_retorna200ConData() throws Exception {
		when(clientService.getClientDetail(1L))
			.thenReturn(new ClientDetailDto(1L, "Sodimac", true, null, null, List.of()));

		mockMvc.perform(get("/clients/1").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Sodimac"));
	}

	@Test
	void detail_registroNoExiste_retorna404() throws Exception {
		when(clientService.getClientDetail(99L))
			.thenThrow(new ResourceNotFoundException("Mandante no encontrado."));

		mockMvc.perform(get("/clients/99").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.message").value("Mandante no encontrado."));
	}

	@Test
	void update_conDatosValidos_retorna200ConDetalleActualizado() throws Exception {
		when(clientService.getClientDetail(1L))
			.thenReturn(new ClientDetailDto(1L, "Falabella", true, null, null, List.of()));

		mockMvc.perform(put("/clients/1")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Falabella\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Falabella"));
	}

	@Test
	void toggleActive_retorna200ConFlagAlternado() throws Exception {
		when(clientService.toggleClientActive(1L))
			.thenReturn(new ClientDetailDto(1L, "Sodimac", false, null, null, List.of()));

		mockMvc.perform(patch("/clients/1/active").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.active").value(false));
	}

	// --- Autorizacion por permiso (@PreAuthorize) ---
	// Cada endpoint exige su authority exacta: sin permiso -> 403 con el shape del contrato; con el
	// permiso exacto -> 2xx. create_conPermisoDeOtraAccion prueba que no basta con tener "algun"
	// permiso de client.

	@Test
	void list_sinPermiso_retorna403ConShapeDeContrato() throws Exception {
		mockMvc.perform(get("/clients").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403))
			.andExpect(jsonPath("$.error.name").value("FORBIDDEN"))
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void detail_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/clients/1").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void create_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(post("/clients")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Sodimac\"}"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void update_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(put("/clients/1")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Falabella\"}"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void toggleActive_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(patch("/clients/1/active").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void create_conPermisoDeOtraAccion_retorna403() throws Exception {
		mockMvc.perform(post("/clients")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("client.read"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Sodimac\"}"))
			.andExpect(status().isForbidden());
	}

	@Test
	void list_conPermisoExacto_retorna200() throws Exception {
		when(clientService.listClients(any(), any())).thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/clients").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("client.read")))
			.andExpect(status().isOk());
	}

	@Test
	void detail_conPermisoExacto_retorna200() throws Exception {
		when(clientService.getClientDetail(1L))
			.thenReturn(new ClientDetailDto(1L, "Sodimac", true, null, null, List.of()));

		mockMvc.perform(get("/clients/1").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("client.read")))
			.andExpect(status().isOk());
	}

	@Test
	void create_conPermisoExacto_retorna201() throws Exception {
		when(clientService.createClient(any())).thenReturn(10L);

		mockMvc.perform(post("/clients")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("client.create"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Sodimac\"}"))
			.andExpect(status().isCreated());
	}

	@Test
	void update_conPermisoExacto_retorna200() throws Exception {
		when(clientService.getClientDetail(1L))
			.thenReturn(new ClientDetailDto(1L, "Falabella", true, null, null, List.of()));

		mockMvc.perform(put("/clients/1")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("client.update"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Falabella\"}"))
			.andExpect(status().isOk());
	}

	@Test
	void toggleActive_conPermisoExacto_retorna200() throws Exception {
		when(clientService.toggleClientActive(1L))
			.thenReturn(new ClientDetailDto(1L, "Sodimac", false, null, null, List.of()));

		mockMvc.perform(patch("/clients/1/active").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("client.active")))
			.andExpect(status().isOk());
	}
}
