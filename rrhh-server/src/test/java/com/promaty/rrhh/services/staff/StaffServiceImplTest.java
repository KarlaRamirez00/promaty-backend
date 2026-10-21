package com.promaty.rrhh.services.staff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.promaty.rrhh.dto.staff.CreateStaffDto;
import com.promaty.rrhh.dto.staff.StaffDetailDto;
import com.promaty.rrhh.dto.staff.StaffFilterParams;
import com.promaty.rrhh.dto.staff.StaffListDto;
import com.promaty.rrhh.dto.staff.StaffSelectorOptionDto;
import com.promaty.rrhh.dto.staff.UpdateStaffDto;
import com.promaty.rrhh.dto.staff.UpdateStaffStatusDto;
import com.promaty.rrhh.entity.Afp;
import com.promaty.rrhh.entity.Bank;
import com.promaty.rrhh.entity.Comuna;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.EducationLevel;
import com.promaty.rrhh.entity.HealthSystem;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.entity.MaritalStatus;
import com.promaty.rrhh.entity.Nationality;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Provincia;
import com.promaty.rrhh.entity.Region;
import com.promaty.rrhh.entity.RegisteredSex;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.projection.StaffContractStartDateProjection;
import com.promaty.rrhh.repository.projection.StaffCostCenterProjection;
import com.promaty.rrhh.services.shared.ActionsResolver;
import com.promaty.rrhh.services.staff.business.builder.CreateStaffBuilder;
import com.promaty.rrhh.services.staff.business.builder.StaffRelationsResolver;
import com.promaty.rrhh.services.staff.business.builder.UpdateStaffBuilder;
import com.promaty.rrhh.services.staff.business.validation.StaffValidation;

@ExtendWith(MockitoExtension.class)
class StaffServiceImplTest {

	@Mock
	private StaffRepository staffRepository;
	@Mock
	private ContractRepository contractRepository;
	@Mock
	private StaffValidation staffValidation;
	@Mock
	private CreateStaffBuilder createStaffBuilder;
	@Mock
	private UpdateStaffBuilder updateStaffBuilder;
	@Mock
	private StaffRelationsResolver relationsResolver;
	@Mock
	private ActionsResolver actionsResolver;

	@InjectMocks
	private StaffServiceImpl service;

	@Test
	void createStaff_conDatosValidos_validaConstruyeGuardaYRetornaId() {
		CreateStaffDto dto = new CreateStaffDto();
		Staff construido = new Staff();
		Staff guardado = staffConId(5L);
		when(createStaffBuilder.build(dto)).thenReturn(construido);
		when(staffRepository.save(construido)).thenReturn(guardado);

		Long id = service.createStaff(dto);

		assertThat(id).isEqualTo(5L);
		verify(staffValidation).validateCreate(dto);
	}

	@Test
	void updateStaff_registroNoExiste_lanzaResourceNotFoundException() {
		UpdateStaffDto dto = new UpdateStaffDto();
		when(staffRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateStaff(1L, dto))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void updateStaff_conDatosValidos_validaAplicaBuilderYGuarda() {
		UpdateStaffDto dto = new UpdateStaffDto();
		Staff existente = staffConId(1L);
		when(staffRepository.findById(1L)).thenReturn(Optional.of(existente));

		service.updateStaff(1L, dto);

		verify(staffValidation).validateUpdate(dto);
		verify(updateStaffBuilder).apply(existente, dto);
		verify(staffRepository).save(existente);
	}

	@Test
	void updateStaffStatus_aBlocked_guardaMotivoYEstado() {
		Staff existente = staffCompleto();
		UpdateStaffStatusDto dto = new UpdateStaffStatusDto();
		dto.setStatusId(99L);
		dto.setBlockReason("Agresión a un compañero de trabajo.");
		PlatformStatus bloqueado = new PlatformStatus();
		bloqueado.setId(99L);
		bloqueado.setCode("BLOCKED");
		bloqueado.setName("Bloqueado");
		when(staffRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(relationsResolver.resolveStatus(99L)).thenReturn(bloqueado);
		when(contractRepository.findByStaffIdAndStatus_Code(1L, "ACTIVE")).thenReturn(Optional.empty());
		when(staffRepository.save(existente)).thenReturn(existente);

		StaffDetailDto detalle = service.updateStaffStatus(1L, dto);

		assertThat(detalle.getStatus().getCode()).isEqualTo("BLOCKED");
		assertThat(detalle.getBlockReason()).isEqualTo("Agresión a un compañero de trabajo.");
		verify(staffValidation).validateStatusChange(dto);
	}

	@Test
	void updateStaffStatus_aActive_limpiaMotivoDeBloqueoAnterior() {
		Staff existente = staffCompleto();
		existente.setBlockReason("Motivo anterior.");
		UpdateStaffStatusDto dto = new UpdateStaffStatusDto();
		dto.setStatusId(11L);
		PlatformStatus activo = new PlatformStatus();
		activo.setId(11L);
		activo.setCode("ACTIVE");
		activo.setName("Activo");
		when(staffRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(relationsResolver.resolveStatus(11L)).thenReturn(activo);
		when(contractRepository.findByStaffIdAndStatus_Code(1L, "ACTIVE")).thenReturn(Optional.empty());
		when(staffRepository.save(existente)).thenReturn(existente);

		StaffDetailDto detalle = service.updateStaffStatus(1L, dto);

		assertThat(detalle.getBlockReason()).isNull();
	}

	@Test
	void getStaffDetail_registroNoExiste_lanzaResourceNotFoundException() {
		when(staffRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getStaffDetail(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getStaffDetail_sinContratoActivo_costCenterCodeEsNulo() {
		when(staffRepository.findById(1L)).thenReturn(Optional.of(staffCompleto()));
		when(contractRepository.findByStaffIdAndStatus_Code(1L, "ACTIVE")).thenReturn(Optional.empty());

		StaffDetailDto detalle = service.getStaffDetail(1L);

		assertThat(detalle.getIdentificationNumber()).isEqualTo("12345678-5");
		assertThat(detalle.getRegisteredSex().getCode()).isEqualTo("MALE");
		assertThat(detalle.getBank().getCode()).isEqualTo("BANCO_ESTADO");
		assertThat(detalle.getCostCenterCode()).isNull();
	}

	@Test
	void getStaffDetail_conContratoActivo_resuelveCostCenterCodeDelProyecto() {
		when(staffRepository.findById(1L)).thenReturn(Optional.of(staffCompleto()));
		Project project = new Project();
		project.setCostCenterCode("00824");
		Contract contrato = new Contract();
		contrato.setProject(project);
		when(contractRepository.findByStaffIdAndStatus_Code(1L, "ACTIVE")).thenReturn(Optional.of(contrato));

		StaffDetailDto detalle = service.getStaffDetail(1L);

		assertThat(detalle.getCostCenterCode()).isEqualTo("00824");
	}

	@Test
	void getStaffDetail_conContratoRechazado_hireDateEsNulo() {
		when(staffRepository.findById(1L)).thenReturn(Optional.of(staffCompleto()));
		when(contractRepository.findByStaffIdAndStatus_Code(1L, "ACTIVE")).thenReturn(Optional.empty());
		when(contractRepository.findFirstByStaffIdAndStatus_CodeInOrderByStartDateDesc(1L, List.of("ACTIVE", "EXPIRED", "ANNULLED")))
			.thenReturn(Optional.empty());

		StaffDetailDto detalle = service.getStaffDetail(1L);

		assertThat(detalle.getHireDate()).isNull();
	}

	@Test
	void getStaffDetail_conContratoQueLlegoAActive_resuelveHireDate() {
		when(staffRepository.findById(1L)).thenReturn(Optional.of(staffCompleto()));
		when(contractRepository.findByStaffIdAndStatus_Code(1L, "ACTIVE")).thenReturn(Optional.empty());
		Contract contratoVencido = new Contract();
		contratoVencido.setStartDate(LocalDate.of(2025, 6, 1));
		when(contractRepository.findFirstByStaffIdAndStatus_CodeInOrderByStartDateDesc(1L, List.of("ACTIVE", "EXPIRED", "ANNULLED")))
			.thenReturn(Optional.of(contratoVencido));

		StaffDetailDto detalle = service.getStaffDetail(1L);

		assertThat(detalle.getHireDate()).isEqualTo(LocalDate.of(2025, 6, 1));
	}

	@Test
	void listSelectorOptionsForContract_delegaEnRepositoryYMapeaNombreCompleto() {
		Staff staff = staffCompleto();
		when(staffRepository.findAvailableForContractSelector("ACTIVE", "BLOCKED")).thenReturn(List.of(staff));

		List<StaffSelectorOptionDto> opciones = service.listSelectorOptionsForContract();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getName()).isEqualTo("Juan Perez Soto");
		assertThat(opciones.get(0).getIdentificationNumber()).isEqualTo("12345678-5");
	}

	@Test
	void listStaff_conResultados_resuelveCentroDeCostoYFechaDeIngresoEnLote() {
		Staff staff1 = staffConId(1L);
		staff1.setStatus(estadoActivo());
		Staff staff2 = staffConId(2L);
		staff2.setStatus(estadoActivo());
		Pageable pageable = PageRequest.of(0, 20);
		Page<Staff> pagina = new PageImpl<>(List.of(staff1, staff2), pageable, 2);

		when(staffRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
			.thenReturn(pagina);
		when(contractRepository.findActiveCostCentersByStaffIds(List.of(1L, 2L), "ACTIVE"))
			.thenReturn(List.of(costCenterProjection(1L, "00824")));
		when(contractRepository.findStartDatesByStaffIds(List.of(1L, 2L), List.of("ACTIVE", "EXPIRED", "ANNULLED")))
			.thenReturn(List.of(
				startDateProjection(2L, LocalDate.of(2025, 3, 1)),
				startDateProjection(2L, LocalDate.of(2026, 1, 10))
			));

		Page<StaffListDto> resultado = service.listStaff(new StaffFilterParams(), pageable);

		List<StaffListDto> contenido = resultado.getContent();
		assertThat(contenido.get(0).getCostCenterCode()).isEqualTo("00824");
		assertThat(contenido.get(0).getHireDate()).isNull();
		assertThat(contenido.get(1).getCostCenterCode()).isNull();
		assertThat(contenido.get(1).getHireDate()).isEqualTo(LocalDate.of(2026, 1, 10));
		verify(contractRepository, org.mockito.Mockito.times(1)).findActiveCostCentersByStaffIds(List.of(1L, 2L), "ACTIVE");
		verify(contractRepository, org.mockito.Mockito.times(1))
			.findStartDatesByStaffIds(List.of(1L, 2L), List.of("ACTIVE", "EXPIRED", "ANNULLED"));
	}

	private PlatformStatus estadoActivo() {
		PlatformStatus status = new PlatformStatus();
		status.setId(1L);
		status.setCode("ACTIVE");
		status.setName("Activo");
		return status;
	}

	private StaffCostCenterProjection costCenterProjection(Long staffId, String costCenterCode) {
		return new StaffCostCenterProjection() {
			@Override
			public Long getStaffId() {
				return staffId;
			}

			@Override
			public String getCostCenterCode() {
				return costCenterCode;
			}
		};
	}

	private StaffContractStartDateProjection startDateProjection(Long staffId, LocalDate startDate) {
		return new StaffContractStartDateProjection() {
			@Override
			public Long getStaffId() {
				return staffId;
			}

			@Override
			public LocalDate getStartDate() {
				return startDate;
			}
		};
	}

	private Staff staffConId(Long id) {
		Staff staff = new Staff();
		staff.setId(id);
		return staff;
	}

	private Staff staffCompleto() {
		Staff staff = staffConId(1L);
		staff.setIdentificationType(IdentificationType.RUT);
		staff.setIdentificationNumber("12345678-5");
		staff.setFirstName("Juan");
		staff.setPaternalLastName("Perez");
		staff.setMaternalLastName("Soto");
		staff.setBirthDate(LocalDate.of(1990, 1, 1));
		staff.setPersonalEmail("juan.perez@example.com");
		staff.setPhone1("912345678");

		RegisteredSex registeredSex = new RegisteredSex();
		registeredSex.setId(1L);
		registeredSex.setName("Masculino");
		registeredSex.setCode("MALE");
		staff.setRegisteredSex(registeredSex);

		MaritalStatus maritalStatus = new MaritalStatus();
		maritalStatus.setId(2L);
		maritalStatus.setName("Soltero/a");
		maritalStatus.setCode("SINGLE");
		staff.setMaritalStatus(maritalStatus);

		Nationality nationality = new Nationality();
		nationality.setId(3L);
		nationality.setName("Chilena");
		nationality.setCode("CHL");
		staff.setNationality(nationality);

		EducationLevel educationLevel = new EducationLevel();
		educationLevel.setId(4L);
		educationLevel.setName("Educación media");
		educationLevel.setCode("HIGH_SCHOOL");
		staff.setEducationLevel(educationLevel);

		Afp afp = new Afp();
		afp.setId(5L);
		afp.setName("AFP Capital");
		afp.setCode("CAPITAL");
		staff.setAfp(afp);

		HealthSystem healthSystem = new HealthSystem();
		healthSystem.setId(6L);
		healthSystem.setName("Fonasa");
		healthSystem.setCode("FONASA");
		staff.setHealthSystem(healthSystem);

		Bank bank = new Bank();
		bank.setId(7L);
		bank.setName("BancoEstado");
		bank.setCode("BANCO_ESTADO");
		staff.setBank(bank);

		Region region = new Region();
		region.setId(8L);
		region.setName("Región Metropolitana de Santiago");
		region.setCode("CL-RM");

		Provincia provincia = new Provincia();
		provincia.setId(9L);
		provincia.setName("Santiago");
		provincia.setCode("rm01");
		provincia.setRegion(region);

		Comuna comuna = new Comuna();
		comuna.setId(10L);
		comuna.setName("Santiago");
		comuna.setCode("rm0101");
		comuna.setProvincia(provincia);
		staff.setComuna(comuna);

		PlatformStatus status = new PlatformStatus();
		status.setId(11L);
		status.setCode("ACTIVE");
		status.setName("Activo");
		staff.setStatus(status);

		return staff;
	}
}
