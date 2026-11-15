package com.promaty.rrhh.services.shared.attachment;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public enum AllowedFileFormat {

	PDF("PDF", List.of("pdf"), List.of("application/pdf"), Signatures.PDF),
	JPG("JPG", List.of("jpg", "jpeg"), List.of("image/jpeg"), Signatures.JPG),
	PNG("PNG", List.of("png"), List.of("image/png"), Signatures.PNG),
	DOC("DOC", List.of("doc"), List.of("application/msword"), Signatures.OLE),
	DOCX("DOCX", List.of("docx"), List.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), Signatures.ZIP),
	XLS("XLS", List.of("xls"), List.of("application/vnd.ms-excel"), Signatures.OLE),
	XLSX("XLSX", List.of("xlsx"), List.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), Signatures.ZIP);

	public static final List<AllowedFileFormat> DOCUMENTS = List.of(PDF, JPG, PNG, DOC, DOCX);
	public static final List<AllowedFileFormat> DOCUMENTS_AND_SPREADSHEETS = List.of(PDF, JPG, PNG, DOC, DOCX, XLS, XLSX);
	public static final int SIGNATURE_LENGTH = 8;

	private final String label;
	private final List<String> extensions;
	private final List<String> contentTypes;
	private final byte[] signature;

	AllowedFileFormat(String label, List<String> extensions, List<String> contentTypes, byte[] signature) {
		this.label = label;
		this.extensions = extensions;
		this.contentTypes = contentTypes;
		this.signature = signature;
	}

	public boolean matches(String extension, String contentType, byte[] header) {
		return extension != null
			&& extensions.contains(extension.toLowerCase(Locale.ROOT))
			&& contentType != null
			&& contentTypes.contains(contentType.toLowerCase(Locale.ROOT))
			&& startsWithSignature(header);
	}

	public String extension() {
		return extensions.get(0);
	}

	public String contentType() {
		return contentTypes.get(0);
	}

	public static String describe(List<AllowedFileFormat> formats) {
		return formats.stream().map(format -> format.label).collect(Collectors.joining(", "));
	}

	private boolean startsWithSignature(byte[] header) {
		return header != null
			&& header.length >= signature.length
			&& Arrays.equals(Arrays.copyOf(header, signature.length), signature);
	}

	private static final class Signatures {
		private static final byte[] PDF = {0x25, 0x50, 0x44, 0x46};
		private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
		private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47};
		// DOC y XLS comparten el contenedor OLE; DOCX y XLSX, el ZIP de Office Open XML.
		private static final byte[] OLE = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0};
		private static final byte[] ZIP = {0x50, 0x4B, 0x03, 0x04};

		private Signatures() {}
	}
}
