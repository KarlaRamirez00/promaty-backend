package com.promaty.rrhh.services.termination.business;

import java.util.List;

import com.promaty.rrhh.entity.LegalTerminationCause;
import com.promaty.rrhh.services.shared.attachment.AllowedFileFormat;
import com.promaty.rrhh.services.shared.attachment.AttachmentRules;
import com.promaty.rrhh.services.shared.attachment.AttachmentSlot;

public final class TerminationAttachmentRules {

	private static final String DEFAULT_LABEL = "Documento de respaldo";

	public static AttachmentRules forCause(LegalTerminationCause cause) {
		String requiredLabel = cause == null ? null : cause.requiredAttachmentLabel();
		AttachmentSlot slot = requiredLabel == null
			? AttachmentSlot.generic(DEFAULT_LABEL, false)
			: AttachmentSlot.generic(requiredLabel, true);
		return new AttachmentRules(List.of(slot), AllowedFileFormat.DOCUMENTS);
	}

	private TerminationAttachmentRules() {}
}
