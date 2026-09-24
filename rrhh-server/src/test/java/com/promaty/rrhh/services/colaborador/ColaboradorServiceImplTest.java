package com.promaty.rrhh.services.colaborador;

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

import com.promaty.rrhh.dto.colaborador.ColaboradorDetailDto;
import com.promaty.rrhh.dto.colaborador.CreateColaboradorDto;
import com.promaty.rrhh.dto.colaborador.UpdateColaboradorDto;
import com.promaty.rrhh.entity.Colaborador;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ColaboradorRepository;
import com.promaty.rrhh.services.colaborador.business.validation.ColaboradorValidation;
import com.promaty.rrhh.services.shared.ActionsResolver;

@ExtendWith(MockitoExtension.class)
class ColaboradorServiceImplTest {

	@Mock
	private ColaboradorRepository colaboradorRepository;
	@Mock
	private ColaboradorValidation colaboradorValidation;
	@Mock
	private ActionsResolver actionsResolver;

	@InjectMocks
	private ColaboradorServiceImpl service;

	@Test
	void createColaborador_conDatosValidos_validaConstruyeGuardaYRetornaId() {
		CreateColaboradorDto dto = createDto();
		Colaborador guardado = colaboradorConId(5L);
		when(colaboradorRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(guardado);

		Long id = service.createColaborador(dto);

		assertThat(id).isEqualTo(5L);
		verify(colaboradorValidation).validateCreate(dto);
	}

	@Test
	void updateColaborador_registroNoExiste_lanzaResourceNotFoundException() {
		UpdateColaboradorDto dto = updateDto();
		when(colaboradorRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateColaborador(1L, dto))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void updateColaborador_conDatosValidos_validaAplicaYGuarda() {
		UpdateColaboradorDto dto = updateDto();
		Colaborador existente = colaboradorConId(1L);
		when(colaboradorRepository.findById(1L)).thenReturn(Optional.of(existente));

		service.updateColaborador(1L, dto);

		verify(colaboradorValidation).validateUpdate(dto);
		assertThat(existente.getFirstName()).isEqualTo(dto.getFirstName());
		verify(colaboradorRepository).save(existente);
	}

	@Test
	void getColaboradorDetail_registroNoExiste_lanzaResourceNotFoundException() {
		when(colaboradorRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getColaboradorDetail(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getColaboradorDetail_registroExiste_retornaDetalleMapeado() {
		when(colaboradorRepository.findById(1L)).thenReturn(Optional.of(colaboradorConId(1L)));

		ColaboradorDetailDto detalle = service.getColaboradorDetail(1L);

		assertThat(detalle.getIdentificationNumber()).isEqualTo("12345678-5");
	}

	@Test
	void toggleColaboradorActive_alternaElFlagYGuarda() {
		Colaborador colaborador = colaboradorConId(1L);
		when(colaboradorRepository.findById(1L)).thenReturn(Optional.of(colaborador));
		when(colaboradorRepository.save(colaborador)).thenReturn(colaborador);

		ColaboradorDetailDto detalle = service.toggleColaboradorActive(1L);

		assertThat(detalle.getActive()).isFalse();
	}

	private CreateColaboradorDto createDto() {
		CreateColaboradorDto dto = new CreateColaboradorDto();
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

	private UpdateColaboradorDto updateDto() {
		UpdateColaboradorDto dto = new UpdateColaboradorDto();
		dto.setFirstName("Juana");
		dto.setPaternalLastName("Perez");
		dto.setMaternalLastName("Soto");
		dto.setBirthDate(LocalDate.of(1990, 1, 1));
		dto.setPersonalEmail("juana.perez@example.com");
		dto.setPhone1("912345678");
		return dto;
	}

	private Colaborador colaboradorConId(Long id) {
		Colaborador colaborador = new Colaborador();
		colaborador.setId(id);
		colaborador.setIdentificationType(IdentificationType.RUT);
		colaborador.setIdentificationNumber("12345678-5");
		colaborador.setFirstName("Juan");
		colaborador.setPaternalLastName("Perez");
		colaborador.setMaternalLastName("Soto");
		colaborador.setBirthDate(LocalDate.of(1990, 1, 1));
		colaborador.setPersonalEmail("juan.perez@example.com");
		colaborador.setPhone1("912345678");
		return colaborador;
	}
}
