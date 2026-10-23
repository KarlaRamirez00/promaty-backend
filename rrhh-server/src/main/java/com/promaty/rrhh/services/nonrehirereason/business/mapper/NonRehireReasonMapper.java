package com.promaty.rrhh.services.nonrehirereason.business.mapper;

import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonDetailDto;
import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonListDto;
import com.promaty.rrhh.entity.NonRehireReason;

public final class NonRehireReasonMapper {

	private NonRehireReasonMapper() {
	}

	public static NonRehireReasonListDto toListDto(NonRehireReason reason) {
		return new NonRehireReasonListDto(
			reason.getId(),
			reason.getName(),
			reason.getCode(),
			reason.getDescription(),
			reason.getActive(),
			reason.getCreatedAt(),
			reason.getUpdatedAt(),
			reason.getCreatedBy(),
			reason.getUpdatedBy(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}

	public static NonRehireReasonDetailDto toDetailDto(NonRehireReason reason) {
		return new NonRehireReasonDetailDto(
			reason.getId(),
			reason.getName(),
			reason.getCode(),
			reason.getDescription(),
			reason.getActive(),
			reason.getCreatedAt(),
			reason.getUpdatedAt(),
			reason.getCreatedBy(),
			reason.getUpdatedBy(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}
}
