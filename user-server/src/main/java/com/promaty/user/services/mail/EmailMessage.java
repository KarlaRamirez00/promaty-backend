package com.promaty.user.services.mail;

import java.util.List;

public record EmailMessage(String to, String subject, String htmlBody, List<InlineImage> inlineImages) {

	public EmailMessage(String to, String subject, String htmlBody) {
		this(to, subject, htmlBody, List.of());
	}
}
