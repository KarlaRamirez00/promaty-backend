package com.promaty.rrhh.controller.staff;

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
import com.promaty.rrhh.dto.staff.RelationSummaryDto;
import com.promaty.rrhh.dto.staff.StaffDetailDto;
import com.promaty.rrhh.dto.staff.StaffListDto;
import com.promaty.rrhh.entity.AccountType;
import com.promaty.rrhh.entity.ClothingSize;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.staff.StaffService;
import com.promaty.rrhh.support.TestJwt;

@WebMvcTest(StaffController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class StaffControllerTest {

	private static final String TOKEN =
		TestJwt.bearer("staff.read", "staff.create", "staff.update");
	private static final String SIN_PERMISOS = TestJwt.bearer();

	private static final String STAFF_JSON = """
		{
		  "identificationType": "RUT",
		  "identificationNumber": "12345678-5",
		  "firstName": "Juan",
		  "paternalLastName": "Perez",
		  "maternalLastName": "Soto",
		  "birthDate": "1990-01-01",
		  "registeredSexId": 1,
		  "maritalStatusId": 1,
		  "nationalityId": 1,
		  "phone1": "912345678",
		  "emergencyPhone": "987654321",
		  "emergencyContactName": "Maria Perez",
		  "address": "Calle Falsa 123",
		  "comunaId": 1,
		  "hasChildren": false,
		  "personalEmail": "juan.perez@example.com",
		  "shoeSize": 42,
		  "clothingSize": "M",
		  "educationLevelId": 1,
		  "afpId": 1,
		  "healthSystemId": 1,
		  "bankId": 1,
		  "accountType": "CHECKING",
		  "accountNumber": "00012345678"
		}
		""";

	private static final String UPDATE_STAFF_JSON = """
		{
		  "firstName": "Juan",
		  "paternalLastName": "Perez",
		  "maternalLastName": "Soto",
		  "birthDate": "1990-01-01",
		  "registeredSexId": 1,
		  "maritalStatusId": 1,
		  "nationalityId": 1,
		  "phone1": "912345678",
		  "emergencyPhone": "987654321",
		  "emergencyContactName": "Maria Perez",
		  "address": "Calle Falsa 123",
		  "comunaId": 1,
		  "hasChildren": false,
		  "personalEmail": "juan.perez@example.com",
		  "shoeSize": 42,
		  "clothingSize": "M",
		  "educationLevelId": 1,
		  "afpId": 1,
		  "healthSystemId": 1,
		  "bankId": 1,
		  "accountType": "CHECKING",
		  "accountNumber": "00012345678"
		}
		""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private StaffService staffService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(staffService.createStaff(any())).thenReturn(10L);

		mockMvc.perform(post("/staff")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(STAFF_JSON))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10));
	}

	@Test
	void create_conRutInvalido_retorna400() throws Exception {
		mockMvc.perform(post("/staff")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(STAFF_JSON.replace("\"phone1\": \"912345678\"", "\"phone1\": \"12345\"")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.phone1").exists());
	}

	@Test
	void list_retorna200ConData() throws Exception {
		Page<StaffListDto> pagina = new PageImpl<>(List.of(
			new StaffListDto(1L, IdentificationType.RUT, "12345678-5", "Juan", "Perez", "Soto",
				null, null, "system", null, List.of())
		));
		when(staffService.listStaff(any(), any())).thenReturn(pagina);

		mockMvc.perform(get("/staff").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].firstName").value("Juan"));
	}

	@Test
	void detail_registroExiste_retorna200() throws Exception {
		when(staffService.getStaffDetail(1L)).thenReturn(detalle());

		mockMvc.perform(get("/staff/1").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.identificationNumber").value("12345678-5"));
	}

	@Test
	void detail_registroNoExiste_retorna404() throws Exception {
		when(staffService.getStaffDetail(99L))
			.thenThrow(new ResourceNotFoundException("Colaborador no encontrado."));

		mockMvc.perform(get("/staff/99").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isNotFound());
	}

	@Test
	void update_conDatosValidos_retorna200() throws Exception {
		when(staffService.getStaffDetail(1L)).thenReturn(detalle());

		mockMvc.perform(put("/staff/1")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(UPDATE_STAFF_JSON))
			.andExpect(status().isOk());
	}

	@Test
	void list_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/staff").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}

	@Test
	void create_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(post("/staff")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content(STAFF_JSON))
			.andExpect(status().isForbidden());
	}

	private StaffDetailDto detalle() {
		RelationSummaryDto registeredSex = new RelationSummaryDto(1L, "Masculino", "MALE");
		RelationSummaryDto maritalStatus = new RelationSummaryDto(1L, "Soltero/a", "SINGLE");
		RelationSummaryDto nationality = new RelationSummaryDto(1L, "Chilena", "CHL");
		RelationSummaryDto educationLevel = new RelationSummaryDto(1L, "Educación media", "HIGH_SCHOOL");
		RelationSummaryDto afp = new RelationSummaryDto(1L, "AFP Capital", "CAPITAL");
		RelationSummaryDto healthSystem = new RelationSummaryDto(1L, "Fonasa", "FONASA");
		RelationSummaryDto bank = new RelationSummaryDto(1L, "BancoEstado", "BANCO_ESTADO");
		RelationSummaryDto region = new RelationSummaryDto(1L, "Región Metropolitana de Santiago", "CL-RM");
		RelationSummaryDto provincia = new RelationSummaryDto(1L, "Santiago", "rm01");
		RelationSummaryDto comuna = new RelationSummaryDto(1L, "Santiago", "rm0101");
		return new StaffDetailDto(1L, IdentificationType.RUT, "12345678-5", "Juan", "Perez", "Soto",
			LocalDate.of(1990, 1, 1), registeredSex, maritalStatus, nationality, "912345678",
			"987654321", "Maria Perez", "Calle Falsa 123", region, provincia, comuna, false, null,
			"juan.perez@example.com", 42, ClothingSize.M, educationLevel, afp, healthSystem, bank,
			AccountType.CHECKING, "00012345678", null, null, "system", null, List.of());
	}
}
