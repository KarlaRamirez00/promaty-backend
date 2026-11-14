package com.promaty.rrhh.services.annex;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.annex.AnnexDetailDto;
import com.promaty.rrhh.dto.annex.AnnexFilterParams;
import com.promaty.rrhh.dto.annex.AnnexListDto;
import com.promaty.rrhh.dto.annex.UpdateAnnexDto;
import com.promaty.rrhh.dto.file.FileAttachmentDto;

public interface AnnexService {

	Page<AnnexListDto> listAnnexes(AnnexFilterParams filters, Pageable pageable);

	AnnexDetailDto getAnnexDetail(Long id);

	void updateAnnex(Long id, UpdateAnnexDto dto);

	FileAttachmentDto uploadAttachment(Long annexId, MultipartFile file);

	void deleteAttachment(Long annexId, Long attachmentId);
}
