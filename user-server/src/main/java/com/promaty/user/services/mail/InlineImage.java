package com.promaty.user.services.mail;

import org.springframework.core.io.Resource;

public record InlineImage(String contentId, Resource resource, String contentType) {
}
