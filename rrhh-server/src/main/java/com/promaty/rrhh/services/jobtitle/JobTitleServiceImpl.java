package com.promaty.rrhh.services.jobtitle;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.JobTitleRepository;

@Service
public class JobTitleServiceImpl implements JobTitleService {

	private final JobTitleRepository jobTitleRepository;

	public JobTitleServiceImpl(JobTitleRepository jobTitleRepository) {
		this.jobTitleRepository = jobTitleRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return jobTitleRepository.findByActiveTrueOrderByName()
			.stream()
			.map(jobTitle -> new CatalogOptionDto(jobTitle.getId(), jobTitle.getName(), jobTitle.getCode()))
			.toList();
	}
}
