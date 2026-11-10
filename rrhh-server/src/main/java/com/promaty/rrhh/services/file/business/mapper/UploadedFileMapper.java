package com.promaty.rrhh.services.file.business.mapper;

import com.promaty.rrhh.dto.file.FileAttachmentDto;
import com.promaty.rrhh.entity.UploadedFile;

public class UploadedFileMapper {

    public static FileAttachmentDto toDto(UploadedFile file) {
        return new FileAttachmentDto(
            file.getId(),
            file.getFileName(),
            file.getContentType(),
            file.getFileSize(),
            "/files/" + file.getMediaFileId(),
            file.getMediaFileId(),
            file.getUpdatedAt()
        );
    }

    private UploadedFileMapper() {}
}
