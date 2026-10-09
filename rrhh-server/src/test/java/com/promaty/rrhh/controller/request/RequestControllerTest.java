package com.promaty.rrhh.controller.request;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.promaty.rrhh.dto.request.RequestDetailDto;
import com.promaty.rrhh.dto.request.RequestListDto;
import com.promaty.rrhh.entity.RequestAction;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.services.request.RequestService;
import com.promaty.rrhh.support.TestJwt;

@WebMvcTest(RequestController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class RequestControllerTest {

	private static final String TOKEN = TestJwt.bearer("contract.create");
	private static final String TOKEN_APROBAR = TestJwt.bearer("contract.approve");
	private static final String TOKEN_LECTURA = TestJwt.bearer("contract.read");
	private static final String SIN_PERMISOS = TestJwt.bearer();

	private static final String DECIDE_JSON = """
		{
		  "decision": "APPROVED"
		}
		""";

	private static final String REQUEST_JSON = """
		{
		  "entityType": "CONTRACT",
		  "action": "CREATE",
		  "projectId": 1,
		  "contractData": {
		    "staffId": 2,
		    "companyId": 3,
		    "contractTypeId": 4,
		    "jobTitleId": 5,
		    "siteId": 6,
		    "startDate": "2026-01-01",
		    "baseSalary": 850000,
		    "weeklyWorkHours": 45,
		    "workDays": 5
		  }
		}
		""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RequestService requestService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(requestService.createRequest(any())).thenReturn(10L);

		mockMvc.perform(post("/requests")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(REQUEST_JSON))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10));
	}

	@Test
	void create_sinProjectId_retorna400() throws Exception {
		mockMvc.perform(post("/requests")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(REQUEST_JSON.replace("\"projectId\": 1,", "")))
			.andExpect(status().isBadRequest());
	}

	@Test
	void create_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(post("/requests")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content(REQUEST_JSON))
			.andExpect(status().isForbidden());
	}

	@Test
	void decide_conPermisoDeAprobar_retorna200() throws Exception {
		doNothing().when(requestService).decideRequest(eq(1L), any());

		mockMvc.perform(patch("/requests/1/decide")
				.header(HttpHeaders.AUTHORIZATION, TOKEN_APROBAR)
				.contentType(MediaType.APPLICATION_JSON)
				.content(DECIDE_JSON))
			.andExpect(status().isOk());
	}

	@Test
	void decide_sinPermisoDeDecision_retorna403() throws Exception {
		mockMvc.perform(patch("/requests/1/decide")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content(DECIDE_JSON))
			.andExpect(status().isForbidden());
	}

	@Test
	void decide_sinDecision_retorna400() throws Exception {
		mockMvc.perform(patch("/requests/1/decide")
				.header(HttpHeaders.AUTHORIZATION, TOKEN_APROBAR)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void list_conPermisoDeLectura_retorna200ConLista() throws Exception {
		RequestListDto dto = new RequestListDto(
			1L, RequestEntityType.CONTRACT, RequestAction.CREATE, null, "Edificio Centro",
			7L, "Pendiente de aprobación", null, null, null, null, List.of()
		);
		when(requestService.listRequests(any(), any())).thenReturn(new PageImpl<>(List.of(dto)));

		mockMvc.perform(get("/requests")
				.header(HttpHeaders.AUTHORIZATION, TOKEN_LECTURA))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].id").value(1))
			.andExpect(jsonPath("$.data[0].projectName").value("Edificio Centro"));
	}

	@Test
	void list_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/requests")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}

	@Test
	void detail_conPermisoDeLectura_retorna200ConDetalle() throws Exception {
		RequestDetailDto dto = new RequestDetailDto(
			1L, RequestEntityType.CONTRACT, RequestAction.CREATE, null, "{}", 7L, "Edificio Centro",
			7L, null, List.of(), null, null, null, null, List.of()
		);
		when(requestService.getRequestDetail(1L)).thenReturn(dto);

		mockMvc.perform(get("/requests/1")
				.header(HttpHeaders.AUTHORIZATION, TOKEN_LECTURA))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(1));
	}

	@Test
	void detail_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/requests/1")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}
}
