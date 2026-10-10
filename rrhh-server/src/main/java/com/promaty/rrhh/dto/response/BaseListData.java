package com.promaty.rrhh.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

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

	public static <T> BaseListData<T> of(Page<T> page) {
		return new BaseListData<>(page.getContent(), Meta.of(paginationDe(page)));
	}

	public static <T> BaseListData<T> of(Page<T> page, Object otherData) {
		return new BaseListData<>(page.getContent(), Meta.of(paginationDe(page), otherData));
	}

	private static Pagination paginationDe(Page<?> page) {
		return new Pagination(
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages()
		);
	}

	// Lista sin paginar (ej. un selector). Meta.empty() + @JsonInclude(NON_NULL) en Meta serializa
	// como "meta": {}, sin nodo pagination.
	public static <T> BaseListData<T> of(List<T> items) {
		return new BaseListData<>(items, Meta.empty());
	}
}
