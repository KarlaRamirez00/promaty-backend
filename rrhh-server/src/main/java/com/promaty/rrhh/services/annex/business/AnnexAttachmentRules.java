package com.promaty.rrhh.services.annex.business;

import java.util.List;

import com.promaty.rrhh.services.shared.attachment.AllowedFileFormat;
import com.promaty.rrhh.services.shared.attachment.AttachmentRules;
import com.promaty.rrhh.services.shared.attachment.AttachmentSlot;

public final class AnnexAttachmentRules {

	private static final AttachmentRules RULES = new AttachmentRules(
		List.of(AttachmentSlot.generic("Documento del anexo", false)),
		AllowedFileFormat.DOCUMENTS
	);

	public static AttachmentRules rules() {
		return RULES;
	}

	private AnnexAttachmentRules() {}
}
