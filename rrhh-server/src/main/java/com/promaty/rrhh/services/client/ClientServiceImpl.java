package com.promaty.rrhh.services.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.client.ClientDetailDto;
import com.promaty.rrhh.dto.client.ClientFilterParams;
import com.promaty.rrhh.dto.client.ClientListDto;
import com.promaty.rrhh.dto.client.CreateClientDto;
import com.promaty.rrhh.dto.client.UpdateClientDto;
import com.promaty.rrhh.entity.Client;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ClientRepository;
import com.promaty.rrhh.services.client.business.builder.ClientQueryBuilder;
import com.promaty.rrhh.services.client.business.mapper.ClientMapper;
import com.promaty.rrhh.services.client.business.validation.ClientValidation;

@Service
public class ClientServiceImpl implements ClientService {

	private static final String NO_ENCONTRADO = "Mandante no encontrado.";

	private final ClientRepository clientRepository;
	private final ClientValidation clientValidation;

	public ClientServiceImpl(ClientRepository clientRepository, ClientValidation clientValidation) {
		this.clientRepository = clientRepository;
		this.clientValidation = clientValidation;
	}

	@Override
	@Transactional
	public Long createClient(CreateClientDto dto) {
		clientValidation.validateCreate(dto);
		Client client = new Client();
		client.setName(dto.getName());
		return clientRepository.save(client).getId();
	}

	@Override
	@Transactional
	public void updateClient(Long id, UpdateClientDto dto) {
		clientValidation.validateUpdate(id, dto);
		Client existente = buscarPorId(id);
		existente.setName(dto.getName());
		clientRepository.save(existente);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ClientListDto> listClients(ClientFilterParams filters, Pageable pageable) {
		Specification<Client> especificacion = ClientQueryBuilder.fromFilters(filters);
		return clientRepository.findAll(especificacion, pageable).map(ClientMapper::toListDto);
	}

	@Override
	@Transactional(readOnly = true)
	public ClientDetailDto getClientDetail(Long id) {
		return ClientMapper.toDetailDto(buscarPorId(id));
	}

	@Override
	@Transactional
	public ClientDetailDto toggleClientActive(Long id) {
		Client client = buscarPorId(id);
		client.toggleActive();
		return ClientMapper.toDetailDto(clientRepository.save(client));
	}

	private Client buscarPorId(Long id) {
		return clientRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}
}
