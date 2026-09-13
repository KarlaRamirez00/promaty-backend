package com.promaty.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Meta {

	private Pagination pagination;

	public static Meta empty() {
		return new Meta(null);
	}

	public static Meta of(Pagination pagination) {
		return new Meta(pagination);
	}
}
