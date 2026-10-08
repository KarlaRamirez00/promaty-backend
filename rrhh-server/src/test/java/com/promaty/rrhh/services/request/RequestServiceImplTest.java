package com.promaty.rrhh.services.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.services.request.business.builder.CreateRequestBuilder;
import com.promaty.rrhh.services.request.business.validation.RequestValidation;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

	@Mock
	private RequestRepository requestRepository;
	@Mock
	private RequestValidation requestValidation;
	@Mock
	private CreateRequestBuilder createRequestBuilder;

	@InjectMocks
	private RequestServiceImpl requestService;

	@Test
	void createRequest_validaArmaYGuarda_retornaIdGenerado() {
		CreateRequestDto dto = new CreateRequestDto();
		Request request = new Request();
		Request guardado = new Request();
		guardado.setId(10L);

		when(createRequestBuilder.build(dto)).thenReturn(request);
		when(requestRepository.save(request)).thenReturn(guardado);

		Long id = requestService.createRequest(dto);

		assertThat(id).isEqualTo(10L);
		verify(requestValidation).validateCreate(dto);
	}
}
