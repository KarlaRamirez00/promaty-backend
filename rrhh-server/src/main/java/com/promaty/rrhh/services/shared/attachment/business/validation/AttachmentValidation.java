package com.promaty.rrhh.services.shared.attachment.business.validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.file.AttachmentItemDto;
import com.promaty.rrhh.entity.UploadedFile;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.services.shared.attachment.AllowedFileFormat;
import com.promaty.rrhh.services.shared.attachment.AttachmentPlan;
import com.promaty.rrhh.services.shared.attachment.AttachmentRules;
import com.promaty.rrhh.services.shared.attachment.AttachmentSlot;

@Component
public class AttachmentValidation {

	private static final String VALIDATION_MESSAGE = "La validacion de adjuntos fallo.";
	private static final String FIELD_ATTACHMENTS = "attachments";
	private static final String FIELD_FILES = "files";
	private static final int MAX_NAME_LENGTH = 255;

	public AttachmentPlan validate(
		AttachmentRules rules,
		List<UploadedFile> existing,
		List<AttachmentItemDto> items,
		List<MultipartFile> files
	) {
		List<MultipartFile> sentFiles = files == null ? List.of() : files;
		Map<String, String> errors = new LinkedHashMap<>();
		AttachmentPlan plan = items == null
			? keepAll(existing, sentFiles, errors)
			: buildPlan(rules, existing, items, sentFiles, errors);
		collectSlotErrors(rules, finalTypes(plan), errors);
		throwIfErrors(errors);
		return plan;
	}

	public void validateRequired(AttachmentRules rules, List<UploadedFile> existing) {
		Map<String, String> errors = new LinkedHashMap<>();
		collectSlotErrors(rules, existing.stream().map(UploadedFile::getAttachmentType).toList(), errors);
		throwIfErrors(errors);
	}

	public long countForSlot(AttachmentSlot slot, List<String> types) {
		return slot.isGeneric() ? types.size() : types.stream().filter(slot.type()::equals).count();
	}

	private AttachmentPlan keepAll(List<UploadedFile> existing, List<MultipartFile> sentFiles, Map<String, String> errors) {
		if (!sentFiles.isEmpty()) {
			errors.put(FIELD_FILES, "Se enviaron archivos sin indicarlos en la lista de adjuntos.");
		}
		List<AttachmentPlan.Kept> kept = existing.stream()
			.map(file -> new AttachmentPlan.Kept(file, file.getFileName()))
			.toList();
		return new AttachmentPlan(kept, List.of(), List.of());
	}

	private AttachmentPlan buildPlan(
		AttachmentRules rules,
		List<UploadedFile> existing,
		List<AttachmentItemDto> items,
		List<MultipartFile> sentFiles,
		Map<String, String> errors
	) {
		Map<Long, UploadedFile> existingById = existing.stream()
			.collect(Collectors.toMap(UploadedFile::getId, Function.identity()));
		List<AttachmentPlan.Kept> kept = new ArrayList<>();
		List<AttachmentPlan.Added> added = new ArrayList<>();
		Set<Integer> usedIndexes = new HashSet<>();

		for (int i = 0; i < items.size(); i++) {
			String key = FIELD_ATTACHMENTS + "[" + i + "]";
			AttachmentItemDto item = items.get(i);
			if (item == null || (item.getId() == null) == (item.getFileIndex() == null)) {
				errors.put(key, "Cada adjunto debe indicar id (guardado) o fileIndex (nuevo), solo uno de los dos.");
			} else if (item.getId() != null) {
				resolveKept(item, existingById, kept, key, errors).ifPresent(kept::add);
			} else {
				resolveAdded(rules, item, sentFiles, usedIndexes, key, errors).ifPresent(added::add);
			}
		}
		IntStream.range(0, sentFiles.size())
			.filter(index -> !usedIndexes.contains(index))
			.forEach(index -> errors.put(fileKey(index), "Este archivo no está indicado en la lista de adjuntos."));

		Set<Long> keptIds = kept.stream().map(entry -> entry.file().getId()).collect(Collectors.toSet());
		List<UploadedFile> removed = existing.stream().filter(file -> !keptIds.contains(file.getId())).toList();
		return new AttachmentPlan(kept, removed, added);
	}

	private Optional<AttachmentPlan.Kept> resolveKept(
		AttachmentItemDto item,
		Map<Long, UploadedFile> existingById,
		List<AttachmentPlan.Kept> alreadyKept,
		String key,
		Map<String, String> errors
	) {
		UploadedFile file = existingById.get(item.getId());
		if (file == null) {
			errors.put(key, "El adjunto indicado no pertenece a este registro.");
			return Optional.empty();
		}
		if (alreadyKept.stream().anyMatch(entry -> entry.file().getId().equals(file.getId()))) {
			errors.put(key, "El adjunto está repetido en la lista.");
			return Optional.empty();
		}
		String name = isBlank(item.getName()) ? file.getFileName() : withExtension(item.getName().trim(), extensionOf(file.getFileName()));
		return validName(name, key, errors).map(valid -> new AttachmentPlan.Kept(file, valid));
	}

	private Optional<AttachmentPlan.Added> resolveAdded(
		AttachmentRules rules,
		AttachmentItemDto item,
		List<MultipartFile> sentFiles,
		Set<Integer> usedIndexes,
		String key,
		Map<String, String> errors
	) {
		int index = item.getFileIndex();
		if (index < 0 || index >= sentFiles.size()) {
			errors.put(key, "fileIndex no corresponde a ningún archivo enviado.");
			return Optional.empty();
		}
		if (!usedIndexes.add(index)) {
			errors.put(key, "El archivo ya está indicado por otro adjunto.");
			return Optional.empty();
		}
		Optional<String> type = resolveType(rules, item.getType(), key, errors);
		MultipartFile file = sentFiles.get(index);
		Optional<AllowedFileFormat> format = detectFormat(rules, file, fileKey(index), errors);
		if (format.isEmpty() || (rules.isTyped() && type.isEmpty())) {
			return Optional.empty();
		}
		String requested = isBlank(item.getName()) ? file.getOriginalFilename() : item.getName().trim();
		String name = withExtension(isBlank(requested) ? "archivo" : requested, format.get().extension());
		return validName(name, key, errors)
			.map(valid -> new AttachmentPlan.Added(file, valid, type.orElse(null), format.get()));
	}

	private Optional<String> resolveType(AttachmentRules rules, String type, String key, Map<String, String> errors) {
		if (!rules.isTyped()) {
			return Optional.empty();
		}
		if (isBlank(type)) {
			errors.put(key, "Indica la casilla (type) del adjunto.");
			return Optional.empty();
		}
		if (rules.slotOfType(type).isEmpty()) {
			errors.put(key, "La casilla " + type + " no existe para este registro.");
			return Optional.empty();
		}
		return Optional.of(type);
	}

	private Optional<AllowedFileFormat> detectFormat(AttachmentRules rules, MultipartFile file, String key, Map<String, String> errors) {
		if (file == null || file.isEmpty()) {
			errors.put(key, "El archivo está vacío.");
			return Optional.empty();
		}
		byte[] header;
		try (InputStream input = file.getInputStream()) {
			header = input.readNBytes(AllowedFileFormat.SIGNATURE_LENGTH);
		} catch (IOException e) {
			errors.put(key, "No se pudo leer el archivo.");
			return Optional.empty();
		}
		String extension = extensionOf(file.getOriginalFilename());
		Optional<AllowedFileFormat> format = rules.formats().stream()
			.filter(candidate -> candidate.matches(extension, file.getContentType(), header))
			.findFirst();
		if (format.isEmpty()) {
			errors.put(key, "Formato no permitido o archivo dañado. Se aceptan: " + AllowedFileFormat.describe(rules.formats()) + ".");
		}
		return format;
	}

	private void collectSlotErrors(AttachmentRules rules, List<String> finalTypes, Map<String, String> errors) {
		List<String> missing = new ArrayList<>();
		List<String> exceeded = new ArrayList<>();
		for (AttachmentSlot slot : rules.slots()) {
			long count = countForSlot(slot, finalTypes);
			if (slot.required() && count == 0) {
				missing.add(slot.label());
			}
			if (count > slot.maxFiles()) {
				exceeded.add(slot.label());
			}
		}
		List<String> messages = new ArrayList<>();
		if (!missing.isEmpty()) {
			messages.add("Falta adjuntar: " + String.join(", ", missing) + ".");
		}
		if (!exceeded.isEmpty()) {
			messages.add("Se permite un solo archivo en: " + String.join(", ", exceeded) + ".");
		}
		if (!messages.isEmpty()) {
			errors.put(FIELD_ATTACHMENTS, String.join(" ", messages));
		}
	}

	private List<String> finalTypes(AttachmentPlan plan) {
		List<String> types = new ArrayList<>();
		plan.kept().forEach(entry -> types.add(entry.file().getAttachmentType()));
		plan.added().forEach(entry -> types.add(entry.type()));
		return types;
	}

	private Optional<String> validName(String name, String key, Map<String, String> errors) {
		if (name.length() > MAX_NAME_LENGTH) {
			errors.put(key, "El nombre del archivo no puede superar los " + MAX_NAME_LENGTH + " caracteres.");
			return Optional.empty();
		}
		return Optional.of(name);
	}

	private String withExtension(String name, String extension) {
		if (extension.isEmpty() || name.toLowerCase(Locale.ROOT).endsWith("." + extension.toLowerCase(Locale.ROOT))) {
			return name;
		}
		return name + "." + extension;
	}

	private String extensionOf(String fileName) {
		if (fileName == null) {
			return "";
		}
		int dot = fileName.lastIndexOf('.');
		return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
	}

	private String fileKey(int index) {
		return FIELD_FILES + "[" + index + "]";
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private void throwIfErrors(Map<String, String> errors) {
		if (!errors.isEmpty()) {
			throw new BusinessValidationException(VALIDATION_MESSAGE, errors);
		}
	}
}
