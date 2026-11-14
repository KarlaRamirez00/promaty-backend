package com.promaty.rrhh.services.shared.attachment;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.file.FileAttachmentDto;
import com.promaty.rrhh.entity.AttachmentEntityType;
import com.promaty.rrhh.entity.UploadedFile;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.UploadedFileRepository;
import com.promaty.rrhh.services.file.business.mapper.UploadedFileMapper;
import com.promaty.rrhh.services.shared.FileStorageService;

@Service
public class AttachmentServiceImpl implements AttachmentService {

	private static final String FIELD_FILE = "file";
	private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
		"application/pdf", "image/jpeg", "image/png",
		"application/msword",
		"application/vnd.openxmlformats-officedocument.wordprocessingml.document"
	);

	private final UploadedFileRepository uploadedFileRepository;
	private final FileStorageService fileStorageService;

	public AttachmentServiceImpl(UploadedFileRepository uploadedFileRepository, FileStorageService fileStorageService) {
		this.uploadedFileRepository = uploadedFileRepository;
		this.fileStorageService = fileStorageService;
	}

	@Override
	public List<FileAttachmentDto> list(AttachmentEntityType entityType, Long entityId) {
		return uploadedFileRepository.findByEntityTypeAndEntityId(entityType, entityId)
			.stream()
			.map(UploadedFileMapper::toDto)
			.toList();
	}

	@Override
	public FileAttachmentDto replaceSingle(AttachmentEntityType entityType, Long entityId, MultipartFile file) {
		validateFile(file);
		List<UploadedFile> existing = uploadedFileRepository.findByEntityTypeAndEntityId(entityType, entityId);
		existing.forEach(uploaded -> fileStorageService.delete(uploaded.getStoragePath()));
		uploadedFileRepository.deleteAll(existing);

		String storagePath = fileStorageService.store(file, entityType.name().toLowerCase(Locale.ROOT) + "/" + entityId);
		UploadedFile uploaded = new UploadedFile();
		uploaded.setMediaFileId(UUID.randomUUID().toString());
		uploaded.setEntityType(entityType);
		uploaded.setEntityId(entityId);
		uploaded.setFileName(file.getOriginalFilename());
		uploaded.setStoragePath(storagePath);
		uploaded.setContentType(file.getContentType());
		uploaded.setFileSize(file.getSize());
		return UploadedFileMapper.toDto(uploadedFileRepository.save(uploaded));
	}

	@Override
	public void delete(AttachmentEntityType entityType, Long entityId, Long attachmentId) {
		UploadedFile uploaded = uploadedFileRepository.findById(attachmentId)
			.filter(file -> file.getEntityType() == entityType && file.getEntityId().equals(entityId))
			.orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado."));
		fileStorageService.delete(uploaded.getStoragePath());
		uploadedFileRepository.delete(uploaded);
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessValidationException("Archivo inválido.", Map.of(FIELD_FILE, "El archivo está vacío."));
		}
		if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
			throw new BusinessValidationException(
				"Tipo de archivo no permitido.",
				Map.of(FIELD_FILE, "Se aceptan: PDF, JPG, PNG, DOC, DOCX.")
			);
		}
	}
}
