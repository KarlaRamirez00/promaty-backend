package com.promaty.rrhh.services.shared.attachment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.promaty.rrhh.dto.file.AttachmentItemDto;
import com.promaty.rrhh.dto.file.AttachmentRequirementDto;
import com.promaty.rrhh.entity.AttachmentEntityType;
import com.promaty.rrhh.entity.UploadedFile;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.UploadedFileRepository;
import com.promaty.rrhh.services.annex.business.AnnexAttachmentRules;
import com.promaty.rrhh.services.contract.business.ContractAttachmentRules;
import com.promaty.rrhh.services.shared.FileStorageService;
import com.promaty.rrhh.services.shared.attachment.business.validation.AttachmentValidation;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

	private static final AttachmentOwner ANNEX = new AttachmentOwner(AttachmentEntityType.ANNEX, 5L);
	private static final AttachmentOwner CONTRACT = new AttachmentOwner(AttachmentEntityType.CONTRACT, 7L);

	@Mock
	private UploadedFileRepository uploadedFileRepository;
	@Mock
	private FileStorageService fileStorageService;

	private AttachmentServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new AttachmentServiceImpl(uploadedFileRepository, fileStorageService, new AttachmentValidation());
	}

	@Test
	void sync_conArchivoNuevo_loGuardaConRutaYFormatoDecididosPorElServidor() {
		when(uploadedFileRepository.findByEntityTypeAndEntityId(AttachmentEntityType.ANNEX, 5L)).thenReturn(List.of());
		when(fileStorageService.store(any(), eq("annex/5"), eq("pdf"))).thenReturn("annex/5/uuid.pdf");

		service.sync(ANNEX, AnnexAttachmentRules.rules(), List.of(nuevo(0, "respaldo", null)), List.of(pdf("original.pdf")));

		ArgumentCaptor<UploadedFile> captor = ArgumentCaptor.forClass(UploadedFile.class);
		verify(uploadedFileRepository).save(captor.capture());
		assertThat(captor.getValue().getFileName()).isEqualTo("respaldo.pdf");
		assertThat(captor.getValue().getStoragePath()).isEqualTo("annex/5/uuid.pdf");
		assertThat(captor.getValue().getContentType()).isEqualTo("application/pdf");
		assertThat(captor.getValue().getEntityId()).isEqualTo(5L);
	}

	@Test
	void sync_conListaNula_noModificaLosAdjuntos() {
		when(uploadedFileRepository.findByEntityTypeAndEntityId(AttachmentEntityType.ANNEX, 5L))
			.thenReturn(List.of(guardado(1L, "acta.pdf", null)));

		service.sync(ANNEX, AnnexAttachmentRules.rules(), null, null);

		verify(uploadedFileRepository, never()).delete(any());
		verify(uploadedFileRepository, never()).save(any());
		verify(fileStorageService, never()).store(any(), any(), any());
	}

	@Test
	void sync_conListaVacia_eliminaLosGuardadosDeLaBaseYDelDisco() {
		UploadedFile existente = guardado(1L, "acta.pdf", null);
		when(uploadedFileRepository.findByEntityTypeAndEntityId(AttachmentEntityType.ANNEX, 5L)).thenReturn(List.of(existente));

		service.sync(ANNEX, AnnexAttachmentRules.rules(), List.of(), List.of());

		verify(uploadedFileRepository).delete(existente);
		verify(fileStorageService).delete("annex/5/1.pdf");
	}

	@Test
	void sync_conNuevoNombre_renombraConservandoLaExtension() {
		UploadedFile existente = guardado(1L, "acta.pdf", null);
		when(uploadedFileRepository.findByEntityTypeAndEntityId(AttachmentEntityType.ANNEX, 5L)).thenReturn(List.of(existente));

		service.sync(ANNEX, AnnexAttachmentRules.rules(), List.of(new AttachmentItemDto(1L, null, "acta firmada", null)), List.of());

		assertThat(existente.getFileName()).isEqualTo("acta firmada.pdf");
		verify(uploadedFileRepository).save(existente);
	}

	@Test
	void sync_conFormatoInvalido_noTocaElDiscoNiLaBase() {
		when(uploadedFileRepository.findByEntityTypeAndEntityId(AttachmentEntityType.ANNEX, 5L)).thenReturn(List.of());
		MockMultipartFile ejecutable = new MockMultipartFile("files", "virus.pdf", "application/pdf", new byte[] {0x4D, 0x5A, 0x00, 0x00});

		assertThatThrownBy(() -> service.sync(ANNEX, AnnexAttachmentRules.rules(), List.of(nuevo(0, null, null)), List.of(ejecutable)))
			.isInstanceOf(BusinessValidationException.class);

		verify(fileStorageService, never()).store(any(), any(), any());
		verify(uploadedFileRepository, never()).save(any());
	}

	@Test
	void requirements_marcaCumplidaSoloLaCasillaQueTieneArchivo() {
		when(uploadedFileRepository.findByEntityTypeAndEntityId(AttachmentEntityType.CONTRACT, 7L))
			.thenReturn(List.of(guardado(1L, "afp.pdf", "CERT_AFP")));

		List<AttachmentRequirementDto> requisitos = service.requirements(CONTRACT, ContractAttachmentRules.rules());

		assertThat(requisitos).hasSize(5);
		assertThat(requisitos).filteredOn(r -> r.getType().equals("CERT_AFP")).singleElement()
			.satisfies(r -> assertThat(r.getFulfilled()).isTrue());
		assertThat(requisitos).filteredOn(r -> !r.getType().equals("CERT_AFP")).allSatisfy(r -> assertThat(r.getFulfilled()).isFalse());
	}

	@Test
	void validateRequired_conCasillasVacias_lanzaFaltaAdjuntar() {
		when(uploadedFileRepository.findByEntityTypeAndEntityId(AttachmentEntityType.CONTRACT, 7L)).thenReturn(List.of());

		assertThatThrownBy(() -> service.validateRequired(CONTRACT, ContractAttachmentRules.rules()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields().get("attachments"))
				.startsWith("Falta adjuntar:")
				.contains("Certificado de AFP"));
	}

	private static AttachmentItemDto nuevo(int fileIndex, String name, String type) {
		return new AttachmentItemDto(null, fileIndex, name, type);
	}

	private static MockMultipartFile pdf(String nombre) {
		return new MockMultipartFile("files", nombre, "application/pdf", new byte[] {0x25, 0x50, 0x44, 0x46, 0x2D});
	}

	private static UploadedFile guardado(Long id, String nombre, String tipo) {
		UploadedFile archivo = new UploadedFile();
		archivo.setId(id);
		archivo.setFileName(nombre);
		archivo.setStoragePath("annex/5/" + id + ".pdf");
		archivo.setAttachmentType(tipo);
		return archivo;
	}
}
