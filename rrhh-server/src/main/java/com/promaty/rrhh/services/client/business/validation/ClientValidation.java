package com.promaty.rrhh.services.client.business.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.client.CreateClientDto;
import com.promaty.rrhh.dto.client.UpdateClientDto;
import com.promaty.rrhh.entity.Client;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.ClientRepository;

@Component
public class ClientValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";

	private final ClientRepository clientRepository;

	public ClientValidation(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

	public void validateCreate(CreateClientDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		if (clientRepository.findByName(dto.getName()).isPresent()) {
			errores.put("name", "Ya existe un mandante con este nombre.");
		}

		lanzarSiHayErrores(errores);
	}

	public void validateUpdate(Long id, UpdateClientDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		Optional<Client> existente = clientRepository.findByName(dto.getName());
		if (existente.isPresent() && !existente.get().getId().equals(id)) {
			errores.put("name", "Ya existe un mandante con este nombre.");
		}

		lanzarSiHayErrores(errores);
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
