package com.promaty.rrhh.services.client;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.promaty.rrhh.dto.client.ClientDetailDto;
import com.promaty.rrhh.dto.client.ClientFilterParams;
import com.promaty.rrhh.dto.client.ClientListDto;
import com.promaty.rrhh.dto.client.CreateClientDto;
import com.promaty.rrhh.dto.client.UpdateClientDto;
import com.promaty.rrhh.dto.shared.NamedOptionDto;

public interface ClientService {

	Long createClient(CreateClientDto dto);

	void updateClient(Long id, UpdateClientDto dto);

	Page<ClientListDto> listClients(ClientFilterParams filters, Pageable pageable);

	ClientDetailDto getClientDetail(Long id);

	ClientDetailDto toggleClientActive(Long id);

	List<NamedOptionDto> listOptions();
}
