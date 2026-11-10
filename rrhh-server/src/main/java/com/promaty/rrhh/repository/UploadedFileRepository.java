package com.promaty.rrhh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.AttachmentEntityType;
import com.promaty.rrhh.entity.UploadedFile;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    List<UploadedFile> findByEntityTypeAndEntityId(AttachmentEntityType entityType, Long entityId);

    Optional<UploadedFile> findByMediaFileId(String mediaFileId);

    long countByEntityTypeAndEntityId(AttachmentEntityType entityType, Long entityId);
}
