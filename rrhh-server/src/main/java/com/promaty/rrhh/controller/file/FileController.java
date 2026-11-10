package com.promaty.rrhh.controller.file;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.entity.UploadedFile;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.UploadedFileRepository;
import com.promaty.rrhh.services.shared.FileStorageService;

@RestController
@RequestMapping("/files")
public class FileController {

    private final UploadedFileRepository uploadedFileRepository;
    private final FileStorageService fileStorageService;

    public FileController(UploadedFileRepository uploadedFileRepository, FileStorageService fileStorageService) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{mediaFileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(@PathVariable String mediaFileId) {
        UploadedFile metadata = uploadedFileRepository.findByMediaFileId(mediaFileId)
            .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado"));

        Resource resource = fileStorageService.load(metadata.getStoragePath());

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + metadata.getFileName() + "\"")
            .header(HttpHeaders.CONTENT_TYPE, metadata.getContentType())
            .body(resource);
    }
}
