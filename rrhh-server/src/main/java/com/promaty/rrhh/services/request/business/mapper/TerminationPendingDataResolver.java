package com.promaty.rrhh.services.request.business.mapper;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.rrhh.dto.contract.RelationSummaryDto;
import com.promaty.rrhh.dto.contract.StaffSummaryDto;
import com.promaty.rrhh.dto.request.TerminationPendingDataDto;
import com.promaty.rrhh.dto.request.TerminationPendingDataResolvedDto;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.repository.LegalTerminationCauseRepository;
import com.promaty.rrhh.repository.NonRehireReasonRepository;
import com.promaty.rrhh.repository.StaffRepository;

@Component
public class TerminationPendingDataResolver {

	private final ObjectMapper objectMapper;
	private final StaffRepository staffRepository;
	private final LegalTerminationCauseRepository legalTerminationCauseRepository;
	private final NonRehireReasonRepository nonRehireReasonRepository;

	public TerminationPendingDataResolver(
		ObjectMapper objectMapper,
		StaffRepository staffRepository,
		LegalTerminationCauseRepository legalTerminationCauseRepository,
		NonRehireReasonRepository nonRehireReasonRepository
	) {
		this.objectMapper = objectMapper;
		this.staffRepository = staffRepository;
		this.legalTerminationCauseRepository = legalTerminationCauseRepository;
		this.nonRehireReasonRepository = nonRehireReasonRepository;
	}

	public TerminationPendingDataResolvedDto resolve(String pendingDataJson) {
		TerminationPendingDataDto datos = deserializar(pendingDataJson);
		return new TerminationPendingDataResolvedDto(
			staffRepository.findById(datos.getStaffId()).map(this::toStaffSummary).orElse(null),
			datos.getTerminationDate(),
			legalTerminationCauseRepository.findById(datos.getLegalTerminationCauseId())
				.map(ltc -> toRelationSummary(ltc.getId(), ltc.getName())).orElse(null),
			datos.getDefinitiveTermination(),
			resolverNonRehireReason(datos.getNonRehireReasonId()),
			datos.getNotes()
		);
	}

	private RelationSummaryDto resolverNonRehireReason(Long id) {
		if (id == null) {
			return null;
		}
		return nonRehireReasonRepository.findById(id).map(nrr -> toRelationSummary(nrr.getId(), nrr.getName())).orElse(null);
	}

	private StaffSummaryDto toStaffSummary(Staff staff) {
		return new StaffSummaryDto(staff.getId(), staff.getFullName(), staff.getIdentificationNumber());
	}

	private RelationSummaryDto toRelationSummary(Long id, String name) {
		return new RelationSummaryDto(id, name);
	}

	private TerminationPendingDataDto deserializar(String pendingData) {
		try {
			return objectMapper.readValue(pendingData, TerminationPendingDataDto.class);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("El pendingData de la solicitud es invalido.", e);
		}
	}
}
