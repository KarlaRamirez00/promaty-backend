package com.promaty.rrhh.services.client.business.mapper;

import com.promaty.rrhh.dto.client.ClientDetailDto;
import com.promaty.rrhh.dto.client.ClientListDto;
import com.promaty.rrhh.entity.Client;

public final class ClientMapper {

	private ClientMapper() {
	}

	public static ClientListDto toListDto(Client client) {
		return new ClientListDto(
			client.getId(),
			client.getName(),
			client.getActive(),
			client.getCreatedAt(),
			client.getUpdatedAt()
		);
	}

	public static ClientDetailDto toDetailDto(Client client) {
		return new ClientDetailDto(
			client.getId(),
			client.getName(),
			client.getActive(),
			client.getCreatedAt(),
			client.getUpdatedAt()
		);
	}
}
