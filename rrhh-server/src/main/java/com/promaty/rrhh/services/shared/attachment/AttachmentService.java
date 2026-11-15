package com.promaty.rrhh.services.shared.attachment;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.file.AttachmentItemDto;
import com.promaty.rrhh.dto.file.AttachmentRequirementDto;
import com.promaty.rrhh.dto.file.FileAttachmentDto;

public interface AttachmentService {

	List<FileAttachmentDto> list(AttachmentOwner owner);

	List<AttachmentRequirementDto> requirements(AttachmentOwner owner, AttachmentRules rules);

	void sync(AttachmentOwner owner, AttachmentRules rules, List<AttachmentItemDto> items, List<MultipartFile> files);

	void validateRequired(AttachmentOwner owner, AttachmentRules rules);
}
