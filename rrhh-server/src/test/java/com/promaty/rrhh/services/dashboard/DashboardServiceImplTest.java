package com.promaty.rrhh.services.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.rrhh.dto.dashboard.BirthdayDto;
import com.promaty.rrhh.dto.dashboard.DashboardSummaryDto;
import com.promaty.rrhh.dto.dashboard.ProjectProgressDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.SettingKey;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.services.setting.SettingProvider;
import com.promaty.security.JwtPrincipal;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

	@Mock
	private ContractRepository contractRepository;
	@Mock
	private StaffRepository staffRepository;
	@Mock
	private TerminationRepository terminationRepository;
	@Mock
	private RequestRepository requestRepository;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private SettingProvider settingProvider;

	@InjectMocks
	private DashboardServiceImpl service;

	@AfterEach
	void limpiarContextoDeSeguridad() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getSummary_sinPermisosDeLectura_dejaTodosLosIndicadoresEnNullSinConsultar() {
		autenticar(false, List.of(1L));

		DashboardSummaryDto resumen = service.getSummary(null);

		assertThat(resumen.getPendingRequests()).isNull();
		assertThat(resumen.getActiveStaff()).isNull();
		assertThat(resumen.getExpiringContracts()).isNull();
		assertThat(resumen.getTerminationsThisMonth()).isNull();
		verifyNoInteractions(contractRepository, staffRepository, terminationRepository, requestRepository);
	}

	@Test
	void getSummary_conContractRead_entregaSoloLosIndicadoresDeContrato() {
		autenticar(false, List.of(1L), "contract.read");
		when(settingProvider.getInt(SettingKey.EXPIRING_CONTRACT_DAYS)).thenReturn(15);
		when(contractRepository.count(any(Specification.class))).thenReturn(5L, 3L, 2L);

		DashboardSummaryDto resumen = service.getSummary(null);

		assertThat(resumen.getExpiringContractsDays()).isEqualTo(15);
		assertThat(resumen.getExpiringContracts()).isEqualTo(5L);
		assertThat(resumen.getHiresThisMonth()).isEqualTo(3L);
		assertThat(resumen.getHiresNextMonth()).isEqualTo(2L);
		assertThat(resumen.getActiveStaff()).isNull();
		assertThat(resumen.getTerminationsThisMonth()).isNull();
	}

	@Test
	void getSummary_conStaffReadYAccesoAcotado_cuentaBloqueadosPorSuContratoMasReciente() {
		autenticar(false, List.of(1L), "staff.read");
		when(contractRepository.count(any(Specification.class))).thenReturn(40L, 2L);

		DashboardSummaryDto resumen = service.getSummary(null);

		assertThat(resumen.getActiveStaff()).isEqualTo(40L);
		assertThat(resumen.getBlockedStaff()).isEqualTo(2L);
		verifyNoInteractions(staffRepository);
	}

	@Test
	void getSummary_conStaffReadYAccesoTotalSinProyecto_cuentaBloqueadosDirectoEnStaff() {
		autenticar(true, List.of(), "staff.read");
		when(contractRepository.count(any(Specification.class))).thenReturn(120L);
		when(staffRepository.count(any(Specification.class))).thenReturn(7L);

		DashboardSummaryDto resumen = service.getSummary(null);

		assertThat(resumen.getActiveStaff()).isEqualTo(120L);
		assertThat(resumen.getBlockedStaff()).isEqualTo(7L);
	}

	@Test
	void getSummary_conTerminationRead_entregaFiniquitosDelMes() {
		autenticar(false, List.of(1L), "termination.read");
		when(terminationRepository.count(any(Specification.class))).thenReturn(4L);

		DashboardSummaryDto resumen = service.getSummary(null);

		assertThat(resumen.getTerminationsThisMonth()).isEqualTo(4L);
	}

	@Test
	void getSummary_conAprobarYValidarContrato_sumaPendientesDeAmbosNivelesYDejaFiniquitoEnNull() {
		autenticar(false, List.of(1L), "contract.approve", "contract.validate");
		when(requestRepository.count(any(Specification.class))).thenReturn(3L, 1L);

		DashboardSummaryDto resumen = service.getSummary(null);

		assertThat(resumen.getPendingRequests().getContract()).isEqualTo(4L);
		assertThat(resumen.getPendingRequests().getTermination()).isNull();
		assertThat(resumen.getPendingRequests().getAnnex()).isNull();
		verify(requestRepository, times(2)).count(any(Specification.class));
	}

	@Test
	void getSummary_conProyectoFueraDelAcceso_lanzaAccessDenied() {
		autenticar(false, List.of(1L), "contract.read");

		assertThatThrownBy(() -> service.getSummary(9L)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void listBirthdays_sinMes_mapeaSoloDiaYMesYOrdenaPorDia() {
		autenticar(false, List.of(1L), "staff.read");
		when(contractRepository.findAll(any(Specification.class))).thenReturn(List.of(
			contratoDe(2L, "Pedro", LocalDate.of(1990, 11, 25)),
			contratoDe(1L, "Ana", LocalDate.of(1985, 11, 3))
		));

		List<BirthdayDto> cumpleanos = service.listBirthdays(null, null);

		assertThat(cumpleanos).extracting(BirthdayDto::getStaffId).containsExactly(1L, 2L);
		assertThat(cumpleanos.get(0).getDay()).isEqualTo(3);
		assertThat(cumpleanos.get(0).getMonth()).isEqualTo(11);
		assertThat(cumpleanos.get(0).getCostCenterCode()).isEqualTo("00824");
	}

	@Test
	void listBirthdays_conMesDistintoDelActual_consultaYRetornaLosDeEseMes() {
		autenticar(false, List.of(1L), "staff.read");
		when(contractRepository.findAll(any(Specification.class))).thenReturn(List.of(
			contratoDe(7L, "Hernán", LocalDate.of(1983, 10, 28))
		));

		List<BirthdayDto> cumpleanos = service.listBirthdays(null, 10);

		assertThat(cumpleanos).extracting(BirthdayDto::getStaffId).containsExactly(7L);
		assertThat(cumpleanos.get(0).getDay()).isEqualTo(28);
		assertThat(cumpleanos.get(0).getMonth()).isEqualTo(10);
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, 0, 13})
	void listBirthdays_conMesFueraDeRango_lanzaBusinessValidationExceptionSinConsultar(int month) {
		autenticar(false, List.of(1L), "staff.read");

		assertThatThrownBy(() -> service.listBirthdays(null, month))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields()).containsKey("month"));

		verifyNoInteractions(contractRepository);
	}

	@Test
	void listProjectProgress_calculaElAvanceDeCadaProyectoEnEjecucion() {
		autenticar(false, List.of(1L), "project.read");
		Project project = new Project();
		project.setId(1L);
		project.setCostCenterCode("00824");
		project.setStartDate(LocalDate.of(2020, 1, 1));
		project.setEndDate(LocalDate.of(2020, 12, 31));
		when(projectRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(project));

		List<ProjectProgressDto> avances = service.listProjectProgress(null);

		assertThat(avances).hasSize(1);
		assertThat(avances.get(0).getElapsedPercent()).isEqualTo(100);
		assertThat(avances.get(0).getOverdueDays()).isPositive();
	}

	private Contract contratoDe(Long staffId, String nombre, LocalDate nacimiento) {
		Staff staff = new Staff();
		staff.setId(staffId);
		staff.setFirstName(nombre);
		staff.setPaternalLastName("Soto");
		staff.setMaternalLastName("Rojas");
		staff.setBirthDate(nacimiento);

		Project project = new Project();
		project.setCostCenterCode("00824");

		Contract contract = new Contract();
		contract.setStaff(staff);
		contract.setProject(project);
		return contract;
	}

	private void autenticar(boolean allowedAllProjects, List<Long> projectIds, String... permisos) {
		JwtPrincipal principal = new JwtPrincipal(
			"1", "Editor", "Ana Pérez", List.of(permisos), allowedAllProjects, projectIds, false, 0L
		);
		List<SimpleGrantedAuthority> autoridades = Arrays.stream(permisos).map(SimpleGrantedAuthority::new).toList();
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(principal, null, autoridades)
		);
	}
}
