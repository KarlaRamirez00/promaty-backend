package com.promaty.rrhh.services.shared.attachment.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.file.AttachmentItemDto;
import com.promaty.rrhh.entity.UploadedFile;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.services.annex.business.AnnexAttachmentRules;
import com.promaty.rrhh.services.contract.business.ContractAttachmentRules;
import com.promaty.rrhh.services.shared.attachment.AttachmentPlan;

class AttachmentValidationTest {

	private static final byte[] PDF = {0x25, 0x50, 0x44, 0x46, 0x2D};
	private static final byte[] ZIP = {0x50, 0x4B, 0x03, 0x04, 0x14};
	private static final String DOCX_MIME = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	private static final List<String> CONTRACT_TYPES = List.of("CEDULA_FRONTAL", "CEDULA_REVERSO", "CERT_AFP", "CERT_SALUD", "CERT_ANTECEDENTES");

	private final AttachmentValidation validation = new AttachmentValidation();

	@Test
	void validate_contratoConLosCincoDocumentos_planificaCincoNuevos() {
		List<AttachmentItemDto> items = CONTRACT_TYPES.stream()
			.map(type -> new AttachmentItemDto(null, CONTRACT_TYPES.indexOf(type), null, type))
			.toList();
		List<MultipartFile> files = CONTRACT_TYPES.stream().<MultipartFile>map(type -> archivo(type + ".pdf", "application/pdf", PDF)).toList();

		AttachmentPlan plan = validation.validate(ContractAttachmentRules.rules(), List.of(), items, files);

		assertThat(plan.added()).hasSize(5);
		assertThat(plan.removed()).isEmpty();
	}

	@Test
	void validate_contratoSinCertificadoDeAfp_acumulaElFaltante() {
		List<AttachmentItemDto> items = List.of(new AttachmentItemDto(null, 0, null, "CEDULA_FRONTAL"));

		Map<String, String> errores = erroresDe(() ->
			validation.validate(ContractAttachmentRules.rules(), List.of(), items, List.of(archivo("c.pdf", "application/pdf", PDF))));

		assertThat(errores.get("attachments")).startsWith("Falta adjuntar:").contains("Certificado de AFP").doesNotContain("frente");
	}

	@Test
	void validate_dosArchivosEnLaMismaCasilla_rechazaPorExceso() {
		List<AttachmentItemDto> items = List.of(new AttachmentItemDto(null, 0, null, null), new AttachmentItemDto(null, 1, null, null));
		List<MultipartFile> files = List.of(archivo("a.pdf", "application/pdf", PDF), archivo("b.pdf", "application/pdf", PDF));

		Map<String, String> errores = erroresDe(() -> validation.validate(AnnexAttachmentRules.rules(), List.of(), items, files));

		assertThat(errores.get("attachments")).contains("Se permite un solo archivo");
	}

	@Test
	void validate_itemConIdYFileIndex_marcaElItem() {
		List<AttachmentItemDto> items = List.of(new AttachmentItemDto(1L, 0, null, null));

		Map<String, String> errores = erroresDe(() ->
			validation.validate(AnnexAttachmentRules.rules(), List.of(), items, List.of(archivo("a.pdf", "application/pdf", PDF))));

		assertThat(errores).containsKey("attachments[0]");
	}

	@Test
	void validate_idDeOtroRegistro_noSeIgnoraEnSilencio() {
		List<AttachmentItemDto> items = List.of(new AttachmentItemDto(99L, null, null, null));

		Map<String, String> errores = erroresDe(() -> validation.validate(AnnexAttachmentRules.rules(), List.of(guardado(1L)), items, List.of()));

		assertThat(errores.get("attachments[0]")).contains("no pertenece");
	}

	@Test
	void validate_archivoSinReferencia_marcaElArchivo() {
		List<MultipartFile> files = List.of(archivo("a.pdf", "application/pdf", PDF));

		Map<String, String> errores = erroresDe(() -> validation.validate(AnnexAttachmentRules.rules(), List.of(), List.of(), files));

		assertThat(errores).containsKey("files[0]");
	}

	@Test
	void validate_ejecutableRenombradoAPdf_rechazaPorFirma() {
		List<AttachmentItemDto> items = List.of(new AttachmentItemDto(null, 0, null, null));
		List<MultipartFile> files = List.of(archivo("virus.pdf", "application/pdf", new byte[] {0x4D, 0x5A, 0x00, 0x00}));

		Map<String, String> errores = erroresDe(() -> validation.validate(AnnexAttachmentRules.rules(), List.of(), items, files));

		assertThat(errores.get("files[0]")).contains("Formato no permitido");
	}

	@Test
	void validate_docxValido_seAcepta() {
		List<AttachmentItemDto> items = List.of(new AttachmentItemDto(null, 0, null, null));
		List<MultipartFile> files = List.of(archivo("carta.docx", DOCX_MIME, ZIP));

		AttachmentPlan plan = validation.validate(AnnexAttachmentRules.rules(), List.of(), items, files);

		assertThat(plan.added()).singleElement().satisfies(added -> assertThat(added.name()).isEqualTo("carta.docx"));
	}

	@Test
	void validate_casillaInexistente_marcaElItem() {
		List<AttachmentItemDto> items = List.of(new AttachmentItemDto(null, 0, null, "PASAPORTE"));

		Map<String, String> errores = erroresDe(() ->
			validation.validate(ContractAttachmentRules.rules(), List.of(), items, List.of(archivo("p.pdf", "application/pdf", PDF))));

		assertThat(errores.get("attachments[0]")).contains("PASAPORTE");
	}

	@Test
	void validate_listaNulaConArchivos_rechazaLosArchivosSueltos() {
		Map<String, String> errores = erroresDe(() ->
			validation.validate(AnnexAttachmentRules.rules(), List.of(), null, List.of(archivo("a.pdf", "application/pdf", PDF))));

		assertThat(errores).containsKey("files");
	}

	private static MockMultipartFile archivo(String nombre, String mime, byte[] contenido) {
		return new MockMultipartFile("files", nombre, mime, contenido);
	}

	private static UploadedFile guardado(Long id) {
		UploadedFile archivo = new UploadedFile();
		archivo.setId(id);
		archivo.setFileName("acta.pdf");
		return archivo;
	}

	private static Map<String, String> erroresDe(Runnable accion) {
		BusinessValidationException ex = (BusinessValidationException) catchThrowable(accion::run);
		assertThat(ex).isNotNull();
		return ex.getErrorFields();
	}
}
