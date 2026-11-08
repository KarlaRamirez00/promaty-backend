package com.promaty.authorizer.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Meta {

	private Long retryAfterSeconds;

	public static Meta empty() {
		return new Meta();
	}

	public static Meta retryAfter(long seconds) {
		Meta meta = new Meta();
		meta.setRetryAfterSeconds(seconds);
		return meta;
	}
}
