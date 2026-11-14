package com.promaty.rrhh.services.shared.attachment;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.file.FileAttachmentDto;
import com.promaty.rrhh.entity.AttachmentEntityType;

public interface AttachmentService {

	List<FileAttachmentDto> list(AttachmentEntityType entityType, Long entityId);

	FileAttachmentDto replaceSingle(AttachmentEntityType entityType, Long entityId, MultipartFile file);

	void delete(AttachmentEntityType entityType, Long entityId, Long attachmentId);
}
