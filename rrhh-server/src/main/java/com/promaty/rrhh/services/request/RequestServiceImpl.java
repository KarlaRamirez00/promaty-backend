package com.promaty.rrhh.services.request;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.services.request.business.builder.CreateRequestBuilder;
import com.promaty.rrhh.services.request.business.validation.RequestValidation;

@Service
public class RequestServiceImpl implements RequestService {

	private final RequestRepository requestRepository;
	private final RequestValidation requestValidation;
	private final CreateRequestBuilder createRequestBuilder;

	public RequestServiceImpl(
		RequestRepository requestRepository,
		RequestValidation requestValidation,
		CreateRequestBuilder createRequestBuilder
	) {
		this.requestRepository = requestRepository;
		this.requestValidation = requestValidation;
		this.createRequestBuilder = createRequestBuilder;
	}

	@Override
	@Transactional
	public Long createRequest(CreateRequestDto dto) {
		requestValidation.validateCreate(dto);
		Request request = createRequestBuilder.build(dto);
		return requestRepository.save(request).getId();
	}
}
