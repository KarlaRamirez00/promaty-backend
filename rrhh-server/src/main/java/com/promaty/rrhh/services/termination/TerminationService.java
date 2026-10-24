package com.promaty.rrhh.services.termination;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.promaty.rrhh.dto.termination.TerminationDetailDto;
import com.promaty.rrhh.dto.termination.TerminationFilterParams;
import com.promaty.rrhh.dto.termination.TerminationListDto;
import com.promaty.rrhh.dto.termination.UpdateTerminationDto;

public interface TerminationService {

	Page<TerminationListDto> listTerminations(TerminationFilterParams filters, Pageable pageable);

	TerminationDetailDto getTerminationDetail(Long id);

	void updateTermination(Long id, UpdateTerminationDto dto);
}
