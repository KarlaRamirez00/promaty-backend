package com.promaty.rrhh.services.legalterminationcause;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.legalterminationcause.LegalTerminationCauseOptionDto;
import com.promaty.rrhh.repository.LegalTerminationCauseRepository;

@Service
public class LegalTerminationCauseServiceImpl implements LegalTerminationCauseService {

	private final LegalTerminationCauseRepository legalTerminationCauseRepository;

	public LegalTerminationCauseServiceImpl(LegalTerminationCauseRepository legalTerminationCauseRepository) {
		this.legalTerminationCauseRepository = legalTerminationCauseRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<LegalTerminationCauseOptionDto> listOptions() {
		return legalTerminationCauseRepository.findByActiveTrueOrderByName()
			.stream()
			.map(cause -> new LegalTerminationCauseOptionDto(
				cause.getId(), cause.getName(), cause.getCode(), cause.getRequiresAttachment(), cause.getAttachmentLabel()
			))
			.toList();
	}
}
