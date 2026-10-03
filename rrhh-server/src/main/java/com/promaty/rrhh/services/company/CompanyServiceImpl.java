package com.promaty.rrhh.services.company;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.CompanyRepository;

@Service
public class CompanyServiceImpl implements CompanyService {

	private final CompanyRepository companyRepository;

	public CompanyServiceImpl(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return companyRepository.findByActiveTrueOrderByName()
			.stream()
			.map(company -> new CatalogOptionDto(company.getId(), company.getName(), company.getCode()))
			.toList();
	}
}
