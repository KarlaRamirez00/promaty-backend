package com.promaty.rrhh.services.shared.attachment;

import java.util.Locale;

import com.promaty.rrhh.entity.AttachmentEntityType;

public record AttachmentOwner(AttachmentEntityType entityType, Long entityId) {

	public String storagePrefix() {
		return entityType.name().toLowerCase(Locale.ROOT) + "/" + entityId;
	}
}
