package com.promaty.rrhh.services.nonrehirereason;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.promaty.rrhh.dto.nonrehirereason.CreateNonRehireReasonDto;
import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonDetailDto;
import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonFilterParams;
import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonListDto;
import com.promaty.rrhh.dto.nonrehirereason.UpdateNonRehireReasonDto;

public interface NonRehireReasonService {

	Long createNonRehireReason(CreateNonRehireReasonDto dto);

	void updateNonRehireReason(Long id, UpdateNonRehireReasonDto dto);

	Page<NonRehireReasonListDto> listNonRehireReasons(NonRehireReasonFilterParams filters, Pageable pageable);

	NonRehireReasonDetailDto getNonRehireReasonDetail(Long id);

	NonRehireReasonDetailDto toggleNonRehireReasonActive(Long id);
}
