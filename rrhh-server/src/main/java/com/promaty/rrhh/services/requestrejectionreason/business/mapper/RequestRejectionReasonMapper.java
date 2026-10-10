package com.promaty.rrhh.services.requestrejectionreason.business.mapper;

import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonDetailDto;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonListDto;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonOptionDto;
import com.promaty.rrhh.entity.RequestRejectionReason;

public final class RequestRejectionReasonMapper {

	private RequestRejectionReasonMapper() {
	}

	public static RequestRejectionReasonListDto toListDto(RequestRejectionReason reason) {
		return new RequestRejectionReasonListDto(
			reason.getId(),
			reason.getName(),
			reason.getDescription(),
			reason.getSubModule(),
			reason.getActive(),
			reason.getCreatedAt(),
			reason.getUpdatedAt(),
			reason.getCreatedBy(),
			reason.getUpdatedBy(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}

	public static RequestRejectionReasonDetailDto toDetailDto(RequestRejectionReason reason) {
		return new RequestRejectionReasonDetailDto(
			reason.getId(),
			reason.getName(),
			reason.getDescription(),
			reason.getSubModule(),
			reason.getActive(),
			reason.getCreatedAt(),
			reason.getUpdatedAt(),
			reason.getCreatedBy(),
			reason.getUpdatedBy(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}

	public static RequestRejectionReasonOptionDto toOptionDto(RequestRejectionReason reason) {
		return new RequestRejectionReasonOptionDto(reason.getId(), reason.getName());
	}
}
