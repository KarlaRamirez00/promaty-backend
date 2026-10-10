package com.promaty.rrhh.services.requestrejectionreason;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.promaty.rrhh.dto.requestrejectionreason.CreateRequestRejectionReasonDto;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonDetailDto;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonFilterParams;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonListDto;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonOptionDto;
import com.promaty.rrhh.dto.requestrejectionreason.UpdateRequestRejectionReasonDto;

public interface RequestRejectionReasonService {

	Long createRequestRejectionReason(CreateRequestRejectionReasonDto dto);

	void updateRequestRejectionReason(Long id, UpdateRequestRejectionReasonDto dto);

	Page<RequestRejectionReasonListDto> listRequestRejectionReasons(RequestRejectionReasonFilterParams filters, Pageable pageable);

	RequestRejectionReasonDetailDto getRequestRejectionReasonDetail(Long id);

	RequestRejectionReasonDetailDto toggleRequestRejectionReasonActive(Long id);

	List<RequestRejectionReasonOptionDto> listOptionsBySubModule(String subModule);
}
