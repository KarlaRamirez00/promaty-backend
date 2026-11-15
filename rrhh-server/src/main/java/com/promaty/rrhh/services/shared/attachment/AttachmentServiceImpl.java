package com.promaty.rrhh.services.shared.attachment;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.file.AttachmentItemDto;
import com.promaty.rrhh.dto.file.AttachmentRequirementDto;
import com.promaty.rrhh.dto.file.FileAttachmentDto;
import com.promaty.rrhh.entity.UploadedFile;
import com.promaty.rrhh.exception.FileStorageException;
import com.promaty.rrhh.repository.UploadedFileRepository;
import com.promaty.rrhh.services.file.business.mapper.UploadedFileMapper;
import com.promaty.rrhh.services.shared.FileStorageService;
import com.promaty.rrhh.services.shared.attachment.business.validation.AttachmentValidation;

@Service
public class AttachmentServiceImpl implements AttachmentService {

	private static final Logger log = LoggerFactory.getLogger(AttachmentServiceImpl.class);

	private final UploadedFileRepository uploadedFileRepository;
	private final FileStorageService fileStorageService;
	private final AttachmentValidation attachmentValidation;

	public AttachmentServiceImpl(
		UploadedFileRepository uploadedFileRepository,
		FileStorageService fileStorageService,
		AttachmentValidation attachmentValidation
	) {
		this.uploadedFileRepository = uploadedFileRepository;
		this.fileStorageService = fileStorageService;
		this.attachmentValidation = attachmentValidation;
	}

	@Override
	public List<FileAttachmentDto> list(AttachmentOwner owner) {
		return findExisting(owner).stream().map(UploadedFileMapper::toDto).toList();
	}

	@Override
	public List<AttachmentRequirementDto> requirements(AttachmentOwner owner, AttachmentRules rules) {
		List<String> types = findExisting(owner).stream().map(UploadedFile::getAttachmentType).toList();
		return rules.slots().stream()
			.map(slot -> new AttachmentRequirementDto(
				slot.type(),
				slot.label(),
				slot.required(),
				slot.maxFiles(),
				attachmentValidation.countForSlot(slot, types) > 0
			))
			.toList();
	}

	@Override
	public void sync(AttachmentOwner owner, AttachmentRules rules, List<AttachmentItemDto> items, List<MultipartFile> files) {
		AttachmentPlan plan = attachmentValidation.validate(rules, findExisting(owner), items, files);
		plan.removed().forEach(this::remove);
		plan.kept().forEach(this::rename);
		plan.added().forEach(added -> store(owner, added));
	}

	@Override
	public void validateRequired(AttachmentOwner owner, AttachmentRules rules) {
		attachmentValidation.validateRequired(rules, findExisting(owner));
	}

	private List<UploadedFile> findExisting(AttachmentOwner owner) {
		return uploadedFileRepository.findByEntityTypeAndEntityId(owner.entityType(), owner.entityId());
	}

	private void remove(UploadedFile file) {
		uploadedFileRepository.delete(file);
		String storagePath = file.getStoragePath();
		afterTransaction(TransactionSynchronization.STATUS_COMMITTED, () -> deleteQuietly(storagePath));
	}

	private void rename(AttachmentPlan.Kept kept) {
		if (!kept.name().equals(kept.file().getFileName())) {
			kept.file().setFileName(kept.name());
			uploadedFileRepository.save(kept.file());
		}
	}

	private void store(AttachmentOwner owner, AttachmentPlan.Added added) {
		String storagePath = fileStorageService.store(added.file(), owner.storagePrefix(), added.format().extension());
		afterTransaction(TransactionSynchronization.STATUS_ROLLED_BACK, () -> deleteQuietly(storagePath));

		UploadedFile uploaded = new UploadedFile();
		uploaded.setMediaFileId(UUID.randomUUID().toString());
		uploaded.setEntityType(owner.entityType());
		uploaded.setEntityId(owner.entityId());
		uploaded.setFileName(added.name());
		uploaded.setStoragePath(storagePath);
		uploaded.setContentType(added.format().contentType());
		uploaded.setFileSize(added.file().getSize());
		uploaded.setAttachmentType(added.type());
		uploadedFileRepository.save(uploaded);
	}

	// Sin transacción activa no hay commit ni rollback que esperar: el efecto solo aplica al confirmar.
	private void afterTransaction(int expectedStatus, Runnable action) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			if (expectedStatus == TransactionSynchronization.STATUS_COMMITTED) {
				action.run();
			}
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status == expectedStatus) {
					action.run();
				}
			}
		});
	}

	private void deleteQuietly(String storagePath) {
		try {
			fileStorageService.delete(storagePath);
		} catch (FileStorageException e) {
			log.warn("No se pudo borrar del disco el archivo {}; queda huérfano sin referencia en la base.", storagePath, e);
		}
	}
}
