package com.promaty.rrhh.services.shared.attachment;

import java.util.List;
import java.util.Optional;

public record AttachmentRules(List<AttachmentSlot> slots, List<AllowedFileFormat> formats) {

	public boolean isTyped() {
		return slots.stream().noneMatch(AttachmentSlot::isGeneric);
	}

	public Optional<AttachmentSlot> slotOfType(String type) {
		return slots.stream().filter(slot -> !slot.isGeneric() && slot.type().equals(type)).findFirst();
	}
}
