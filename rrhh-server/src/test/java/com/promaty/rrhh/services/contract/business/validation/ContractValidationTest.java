package com.promaty.rrhh.services.contract.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.rrhh.dto.contract.UpdateContractDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.ContractType;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.CompanyRepository;
import com.promaty.rrhh.repository.ContractTypeRepository;
import com.promaty.rrhh.repository.JobTitleRepository;
import com.promaty.rrhh.repository.MealTypeRepository;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.repository.SiteRepository;
import com.promaty.rrhh.repository.TransportTypeRepository;
import com.promaty.security.JwtPrincipal;

@ExtendWith(MockitoExtension.class)
class ContractValidationTest {

	private static final Long PROYECTO_PROPIO = 1L;
	private static final Long PROYECTO_AJENO = 2L;
	private static final Long TIPO_INDEFINIDO = 4L;

	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private CompanyRepository companyRepository;
	@Mock
	private ContractTypeRepository contractTypeRepository;
	@Mock
	private JobTitleRepository jobTitleRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private MealTypeRepository mealTypeRepository;
	@Mock
	private TransportTypeRepository transportTypeRepository;

	private ContractValidation validation;

	@BeforeEach
	void setUp() {
		validation = new ContractValidation(
			projectRepository, companyRepository, contractTypeRepository, jobTitleRepository,
			siteRepository, mealTypeRepository, transportTypeRepository
		);
		lenient().when(projectRepository.existsById(anyLong())).thenReturn(true);
		lenient().when(companyRepository.existsById(anyLong())).thenReturn(true);
		lenient().when(contractTypeRepository.existsById(anyLong())).thenReturn(true);
		lenient().when(jobTitleRepository.existsById(anyLong())).thenReturn(true);
		lenient().when(siteRepository.existsById(anyLong())).thenReturn(true);
		lenient().when(contractTypeRepository.findById(TIPO_INDEFINIDO)).thenReturn(Optional.of(tipo("I")));
		autenticarConProyectos(List.of(PROYECTO_PROPIO));
	}

	@AfterEach
	void limpiarContextoDeSeguridad() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void validateUpdate_conDatosValidos_noLanzaExcepcion() {
		assertThatCode(() -> validation.validateUpdate(contrato(LocalDate.now().plusDays(5)), dto(PROYECTO_PROPIO)))
			.doesNotThrowAnyException();
	}

	@Test
	void validateUpdate_proyectoSinAcceso_rechazaElCentroDeCosto() {
		Map<String, String> errores = errores(contrato(LocalDate.now().plusDays(5)), dto(PROYECTO_AJENO));

		assertThat(errores).containsEntry("projectId", "No tienes acceso al centro de costo indicado.");
	}

	@Test
	void validateUpdate_fechaDeInicioPasadaSinCambios_noBloqueaLaCorreccion() {
		LocalDate inicioYaPasado = LocalDate.now().minusDays(3);
		UpdateContractDto dto = dto(PROYECTO_PROPIO);
		dto.setStartDate(inicioYaPasado);

		assertThatCode(() -> validation.validateUpdate(contrato(inicioYaPasado), dto)).doesNotThrowAnyException();
	}

	@Test
	void validateUpdate_cambiaLaFechaDeInicioAUnaPasada_laRechaza() {
		UpdateContractDto dto = dto(PROYECTO_PROPIO);
		dto.setStartDate(LocalDate.now().minusDays(1));

		assertThat(errores(contrato(LocalDate.now().plusDays(5)), dto)).containsKey("startDate");
	}

	@Test
	void validateUpdate_plazoFijoSinFechaDeTermino_laExige() {
		when(contractTypeRepository.findById(7L)).thenReturn(Optional.of(tipo("F")));
		UpdateContractDto dto = dto(PROYECTO_PROPIO);
		dto.setContractTypeId(7L);

		assertThat(errores(contrato(LocalDate.now().plusDays(5)), dto)).containsKey("endDate");
	}

	private Map<String, String> errores(Contract contract, UpdateContractDto dto) {
		return catchThrowableOfType(BusinessValidationException.class, () -> validation.validateUpdate(contract, dto))
			.getErrorFields();
	}

	private static UpdateContractDto dto(Long projectId) {
		UpdateContractDto dto = new UpdateContractDto();
		dto.setProjectId(projectId);
		dto.setCompanyId(3L);
		dto.setContractTypeId(TIPO_INDEFINIDO);
		dto.setJobTitleId(5L);
		dto.setSiteId(6L);
		dto.setStartDate(LocalDate.now().plusDays(5));
		dto.setBaseSalary(BigDecimal.valueOf(900000));
		dto.setWeeklyWorkHours(45);
		dto.setWorkDays(5);
		return dto;
	}

	private static Contract contrato(LocalDate startDate) {
		Contract contract = new Contract();
		contract.setId(1L);
		contract.setStartDate(startDate);
		return contract;
	}

	private static ContractType tipo(String code) {
		ContractType type = new ContractType();
		type.setCode(code);
		return type;
	}

	private static void autenticarConProyectos(List<Long> projectIds) {
		JwtPrincipal principal = new JwtPrincipal("1", "Jefe de obra", "Juan", List.of(), false, projectIds, false, 0L);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
	}
}
