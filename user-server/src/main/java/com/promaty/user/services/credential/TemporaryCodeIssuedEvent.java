package com.promaty.user.services.credential;

import com.promaty.user.entity.OneTimeCodePurpose;

public record TemporaryCodeIssuedEvent(String email, String firstName, String code, OneTimeCodePurpose purpose) {
}
