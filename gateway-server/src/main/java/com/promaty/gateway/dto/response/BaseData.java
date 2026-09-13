package com.promaty.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseData<T> {

	private T data;
	private ErrorResponse error;
	private Meta meta;

	public static <T> BaseData<T> success(T data) {
		return new BaseData<>(data, null, Meta.empty());
	}

	public static <T> BaseData<T> error(ErrorResponse error) {
		return new BaseData<>(null, error, Meta.empty());
	}
}
