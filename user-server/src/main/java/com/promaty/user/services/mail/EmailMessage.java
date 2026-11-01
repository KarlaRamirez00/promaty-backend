package com.promaty.user.services.mail;

public record EmailMessage(String to, String subject, String htmlBody) {
}
