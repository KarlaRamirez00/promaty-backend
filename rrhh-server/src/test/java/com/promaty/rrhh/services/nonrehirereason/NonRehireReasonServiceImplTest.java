package com.promaty.rrhh.services.nonrehirereason;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.nonrehirereason.CreateNonRehireReasonDto;
import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonDetailDto;
import com.promaty.rrhh.dto.nonrehirereason.UpdateNonRehireReasonDto;
import com.promaty.rrhh.entity.NonRehireReason;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.NonRehireReasonRepository;
import com.promaty.rrhh.services.nonrehirereason.business.validation.NonRehireReasonValidation;
import com.promaty.rrhh.services.shared.ActionsResolver;

@ExtendWith(MockitoExtension.class)
class NonRehireReasonServiceImplTest {

	@Mock
	private NonRehireReasonRepository nonRehireReasonRepository;

	@Mock
	private NonRehireReasonValidation nonRehireReasonValidation;

	@Mock
	private ActionsResolver actionsResolver;

	@InjectMocks
	private NonRehireReasonServiceImpl service;

	@Test
	void createNonRehireReason_conDatosValidos_validaGuardaYRetornaId() {
		CreateNonRehireReasonDto dto = new CreateNonRehireReasonDto();
		dto.setName("Bajo desempeño");
		dto.setCode("LOW_PERFORMANCE");
		when(nonRehireReasonRepository.save(any(NonRehireReason.class))).thenReturn(reason(7L, "Bajo desempeño", "LOW_PERFORMANCE", true));

		Long id = service.createNonRehireReason(dto);

		assertThat(id).isEqualTo(7L);
		verify(nonRehireReasonValidation).validateCreate(dto);
	}

	@Test
	void updateNonRehireReason_registroNoExiste_lanzaResourceNotFoundException() {
		UpdateNonRehireReasonDto dto = new UpdateNonRehireReasonDto();
		dto.setName("Bajo desempeño");
		dto.setCode("LOW_PERFORMANCE");
		when(nonRehireReasonRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateNonRehireReason(1L, dto))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getNonRehireReasonDetail_registroNoExiste_lanzaResourceNotFoundException() {
		when(nonRehireReasonRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getNonRehireReasonDetail(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void toggleNonRehireReasonActive_registroActivo_loDesactivaYRetornaDetalle() {
		NonRehireReason existente = reason(1L, "Bajo desempeño", "LOW_PERFORMANCE", true);
		when(nonRehireReasonRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(nonRehireReasonRepository.save(existente)).thenReturn(existente);

		NonRehireReasonDetailDto detalle = service.toggleNonRehireReasonActive(1L);

		assertThat(existente.getActive()).isFalse();
		assertThat(detalle.getActive()).isFalse();
		assertThat(detalle.getCode()).isEqualTo("LOW_PERFORMANCE");
	}

	private NonRehireReason reason(Long id, String name, String code, boolean active) {
		NonRehireReason entidad = new NonRehireReason();
		entidad.setId(id);
		entidad.setName(name);
		entidad.setCode(code);
		entidad.setActive(active);
		return entidad;
	}
}
