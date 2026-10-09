package com.promaty.rrhh.services.request;

import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.request.DecideRequestDto;

public interface RequestService {

	Long createRequest(CreateRequestDto dto);

	void decideRequest(Long id, DecideRequestDto dto);
}
