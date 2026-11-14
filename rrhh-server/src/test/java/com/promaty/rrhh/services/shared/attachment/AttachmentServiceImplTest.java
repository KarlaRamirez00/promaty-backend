package com.promaty.rrhh.services.shared.attachment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.promaty.rrhh.dto.file.FileAttachmentDto;
import com.promaty.rrhh.entity.AttachmentEntityType;
import com.promaty.rrhh.entity.UploadedFile;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.UploadedFileRepository;
import com.promaty.rrhh.services.shared.FileStorageService;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

	@Mock
	private UploadedFileRepository uploadedFileRepository;
	@Mock
	private FileStorageService fileStorageService;

	@InjectMocks
	private AttachmentServiceImpl service;

	@Test
	void replaceSingle_reemplazaElArchivoAnteriorDelMismoDueno() {
		UploadedFile anterior = archivoDe(AttachmentEntityType.ANNEX, 5L, "annex/5/viejo.pdf");
		when(uploadedFileRepository.findByEntityTypeAndEntityId(AttachmentEntityType.ANNEX, 5L)).thenReturn(List.of(anterior));
		when(fileStorageService.store(any(), any())).thenReturn("annex/5/nuevo.pdf");
		when(uploadedFileRepository.save(any(UploadedFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

		FileAttachmentDto resultado = service.replaceSingle(AttachmentEntityType.ANNEX, 5L, pdf());

		verify(fileStorageService).delete("annex/5/viejo.pdf");
		verify(uploadedFileRepository).deleteAll(List.of(anterior));
		verify(fileStorageService).store(any(), any());
		assertThat(resultado.getName()).isEqualTo("respaldo.pdf");
	}

	@Test
	void replaceSingle_conFormatoNoPermitido_noTocaElDisco() {
		MockMultipartFile ejecutable = new MockMultipartFile("file", "virus.exe", "application/x-msdownload", new byte[] {1});

		assertThatThrownBy(() -> service.replaceSingle(AttachmentEntityType.ANNEX, 5L, ejecutable))
			.isInstanceOf(BusinessValidationException.class);

		verify(fileStorageService, never()).store(any(), any());
	}

	@Test
	void delete_conArchivoDeOtroDueno_respondeNoEncontradoSinBorrar() {
		UploadedFile ajeno = archivoDe(AttachmentEntityType.TERMINATION, 5L, "termination/5/x.pdf");
		when(uploadedFileRepository.findById(9L)).thenReturn(Optional.of(ajeno));

		assertThatThrownBy(() -> service.delete(AttachmentEntityType.ANNEX, 5L, 9L))
			.isInstanceOf(ResourceNotFoundException.class);

		verify(fileStorageService, never()).delete(any());
	}

	private MockMultipartFile pdf() {
		return new MockMultipartFile("file", "respaldo.pdf", "application/pdf", new byte[] {1, 2, 3});
	}

	private UploadedFile archivoDe(AttachmentEntityType entityType, Long entityId, String storagePath) {
		UploadedFile file = new UploadedFile();
		file.setEntityType(entityType);
		file.setEntityId(entityId);
		file.setStoragePath(storagePath);
		return file;
	}
}
