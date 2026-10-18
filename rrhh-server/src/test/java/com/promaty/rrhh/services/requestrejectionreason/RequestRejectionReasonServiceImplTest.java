package com.promaty.rrhh.services.requestrejectionreason;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonSubModuleOptionDto;
import com.promaty.rrhh.repository.RequestRejectionReasonRepository;
import com.promaty.rrhh.services.requestrejectionreason.business.validation.RequestRejectionReasonValidation;
import com.promaty.rrhh.services.shared.ActionsResolver;

@ExtendWith(MockitoExtension.class)
class RequestRejectionReasonServiceImplTest {

	@Mock
	private RequestRejectionReasonRepository requestRejectionReasonRepository;
	@Mock
	private RequestRejectionReasonValidation requestRejectionReasonValidation;
	@Mock
	private ActionsResolver actionsResolver;

	@InjectMocks
	private RequestRejectionReasonServiceImpl service;

	@Test
	void listSubModules_retornaElCatalogoCompletoDesdeElEnum() {
		List<RequestRejectionReasonSubModuleOptionDto> opciones = service.listSubModules();

		assertThat(opciones).extracting(RequestRejectionReasonSubModuleOptionDto::getCode)
			.containsExactly("contracts");
		assertThat(opciones).extracting(RequestRejectionReasonSubModuleOptionDto::getName)
			.containsExactly("Contratos");
	}
}
