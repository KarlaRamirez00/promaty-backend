package com.promaty.rrhh.services.shared.attachment;

public record AttachmentSlot(String type, String label, boolean required, int maxFiles) {

	public static final int SINGLE_FILE = 1;

	public static AttachmentSlot generic(String label, boolean required) {
		return new AttachmentSlot(null, label, required, SINGLE_FILE);
	}

	public static AttachmentSlot typed(String type, String label) {
		return new AttachmentSlot(type, label, true, SINGLE_FILE);
	}

	public boolean isGeneric() {
		return type == null;
	}
}
