package com.promaty.rrhh.services.staff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import com.promaty.rrhh.dto.staff.CreateStaffDto;
import com.promaty.rrhh.dto.staff.StaffDetailDto;
import com.promaty.rrhh.dto.staff.StaffSelectorOptionDto;
import com.promaty.rrhh.dto.staff.UpdateStaffDto;
import com.promaty.rrhh.entity.Afp;
import com.promaty.rrhh.entity.Bank;
import com.promaty.rrhh.entity.Comuna;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.EducationLevel;
import com.promaty.rrhh.entity.HealthSystem;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.entity.MaritalStatus;
import com.promaty.rrhh.entity.Nationality;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Provincia;
import com.promaty.rrhh.entity.Region;
import com.promaty.rrhh.entity.RegisteredSex;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.services.shared.ActionsResolver;
import com.promaty.rrhh.services.staff.business.builder.CreateStaffBuilder;
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
	void listSelectorOptionsForContract_delegaEnRepositoryYMapeaNombreCompleto() {
		Staff staff = staffCompleto();
		when(staffRepository.findAvailableForContractSelector("ACTIVE")).thenReturn(List.of(staff));

		List<StaffSelectorOptionDto> opciones = service.listSelectorOptionsForContract();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getName()).isEqualTo("Juan Perez Soto");
		assertThat(opciones.get(0).getIdentificationNumber()).isEqualTo("12345678-5");
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

		return staff;
	}
}
