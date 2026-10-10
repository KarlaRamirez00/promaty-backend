package com.promaty.rrhh.services.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.request.DecideRequestDto;
import com.promaty.rrhh.dto.request.RequestCountersDto;
import com.promaty.rrhh.dto.request.RequestDetailDto;
import com.promaty.rrhh.dto.request.RequestFilterParams;
import com.promaty.rrhh.dto.request.RequestListDto;
import com.promaty.rrhh.entity.RequestEntityType;

public interface RequestService {

	Long createRequest(CreateRequestDto dto);

	void decideRequest(Long id, DecideRequestDto dto);

	Page<RequestListDto> listRequests(RequestFilterParams filters, Pageable pageable);

	RequestDetailDto getRequestDetail(Long id);

	RequestCountersDto getCounters(RequestEntityType entityType);
}
