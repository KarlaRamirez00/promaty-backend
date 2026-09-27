package com.promaty.rrhh.services.staff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.staff.CreateStaffDto;
import com.promaty.rrhh.dto.staff.StaffDetailDto;
import com.promaty.rrhh.dto.staff.UpdateStaffDto;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.services.shared.ActionsResolver;
import com.promaty.rrhh.services.staff.business.validation.StaffValidation;

@ExtendWith(MockitoExtension.class)
class StaffServiceImplTest {

	@Mock
	private StaffRepository staffRepository;
	@Mock
	private StaffValidation staffValidation;
	@Mock
	private ActionsResolver actionsResolver;

	@InjectMocks
	private StaffServiceImpl service;

	@Test
	void createStaff_conDatosValidos_validaConstruyeGuardaYRetornaId() {
		CreateStaffDto dto = createDto();
		Staff guardado = staffConId(5L);
		when(staffRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(guardado);

		Long id = service.createStaff(dto);

		assertThat(id).isEqualTo(5L);
		verify(staffValidation).validateCreate(dto);
	}

	@Test
	void updateStaff_registroNoExiste_lanzaResourceNotFoundException() {
		UpdateStaffDto dto = updateDto();
		when(staffRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateStaff(1L, dto))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void updateStaff_conDatosValidos_validaAplicaYGuarda() {
		UpdateStaffDto dto = updateDto();
		Staff existente = staffConId(1L);
		when(staffRepository.findById(1L)).thenReturn(Optional.of(existente));

		service.updateStaff(1L, dto);

		verify(staffValidation).validateUpdate(dto);
		assertThat(existente.getFirstName()).isEqualTo(dto.getFirstName());
		verify(staffRepository).save(existente);
	}

	@Test
	void getStaffDetail_registroNoExiste_lanzaResourceNotFoundException() {
		when(staffRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getStaffDetail(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getStaffDetail_registroExiste_retornaDetalleMapeado() {
		when(staffRepository.findById(1L)).thenReturn(Optional.of(staffConId(1L)));

		StaffDetailDto detalle = service.getStaffDetail(1L);

		assertThat(detalle.getIdentificationNumber()).isEqualTo("12345678-5");
	}


	private CreateStaffDto createDto() {
		CreateStaffDto dto = new CreateStaffDto();
		dto.setIdentificationType(IdentificationType.RUT);
		dto.setIdentificationNumber("12345678-5");
		dto.setFirstName("Juan");
		dto.setPaternalLastName("Perez");
		dto.setMaternalLastName("Soto");
		dto.setBirthDate(LocalDate.of(1990, 1, 1));
		dto.setPersonalEmail("juan.perez@example.com");
		dto.setPhone1("912345678");
		return dto;
	}

	private UpdateStaffDto updateDto() {
		UpdateStaffDto dto = new UpdateStaffDto();
		dto.setFirstName("Juana");
		dto.setPaternalLastName("Perez");
		dto.setMaternalLastName("Soto");
		dto.setBirthDate(LocalDate.of(1990, 1, 1));
		dto.setPersonalEmail("juana.perez@example.com");
		dto.setPhone1("912345678");
		return dto;
	}

	private Staff staffConId(Long id) {
		Staff staff = new Staff();
		staff.setId(id);
		staff.setIdentificationType(IdentificationType.RUT);
		staff.setIdentificationNumber("12345678-5");
		staff.setFirstName("Juan");
		staff.setPaternalLastName("Perez");
		staff.setMaternalLastName("Soto");
		staff.setBirthDate(LocalDate.of(1990, 1, 1));
		staff.setPersonalEmail("juan.perez@example.com");
		staff.setPhone1("912345678");
		return staff;
	}
}
