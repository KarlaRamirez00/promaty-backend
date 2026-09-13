package com.promaty.gateway.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseListData<T> {

	private List<T> data;
	private Meta meta;

	public static <T> BaseListData<T> of(List<T> items) {
		return new BaseListData<>(items, Meta.empty());
	}
}
