package com.promaty.rrhh.services.contract.business;

import java.util.List;

import com.promaty.rrhh.entity.ContractAttachmentType;
import com.promaty.rrhh.services.shared.attachment.AllowedFileFormat;
import com.promaty.rrhh.services.shared.attachment.AttachmentRules;
import com.promaty.rrhh.services.shared.attachment.AttachmentSlot;

public final class ContractAttachmentRules {

	private static final AttachmentRules RULES = new AttachmentRules(
		List.of(
			AttachmentSlot.typed(ContractAttachmentType.CEDULA_FRONTAL.name(), "Cédula de identidad (frente)"),
			AttachmentSlot.typed(ContractAttachmentType.CEDULA_REVERSO.name(), "Cédula de identidad (reverso)"),
			AttachmentSlot.typed(ContractAttachmentType.CERT_AFP.name(), "Certificado de AFP"),
			AttachmentSlot.typed(ContractAttachmentType.CERT_SALUD.name(), "Certificado de salud"),
			AttachmentSlot.typed(ContractAttachmentType.CERT_ANTECEDENTES.name(), "Certificado de antecedentes")
		),
		AllowedFileFormat.DOCUMENTS_AND_SPREADSHEETS
	);

	public static AttachmentRules rules() {
		return RULES;
	}

	private ContractAttachmentRules() {}
}
