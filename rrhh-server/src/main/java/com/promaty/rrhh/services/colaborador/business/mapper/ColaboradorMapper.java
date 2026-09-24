package com.promaty.rrhh.services.colaborador.business.mapper;

import com.promaty.rrhh.dto.colaborador.ColaboradorDetailDto;
import com.promaty.rrhh.dto.colaborador.ColaboradorListDto;
import com.promaty.rrhh.entity.Colaborador;

public final class ColaboradorMapper {

	private ColaboradorMapper() {
	}

	public static ColaboradorListDto toListDto(Colaborador colaborador) {
		return new ColaboradorListDto(
			colaborador.getId(),
			colaborador.getIdentificationType(),
			colaborador.getIdentificationNumber(),
			colaborador.getFirstName(),
			colaborador.getPaternalLastName(),
			colaborador.getMaternalLastName(),
			colaborador.getActive(),
			colaborador.getCreatedAt(),
			colaborador.getUpdatedAt(),
			colaborador.getCreatedBy(),
			colaborador.getUpdatedBy(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}

	public static ColaboradorDetailDto toDetailDto(Colaborador colaborador) {
		return new ColaboradorDetailDto(
			colaborador.getId(),
			colaborador.getIdentificationType(),
			colaborador.getIdentificationNumber(),
			colaborador.getFirstName(),
			colaborador.getPaternalLastName(),
			colaborador.getMaternalLastName(),
			colaborador.getBirthDate(),
			colaborador.getPersonalEmail(),
			colaborador.getPhone1(),
			colaborador.getActive(),
			colaborador.getCreatedAt(),
			colaborador.getUpdatedAt(),
			colaborador.getCreatedBy(),
			colaborador.getUpdatedBy(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}
}
