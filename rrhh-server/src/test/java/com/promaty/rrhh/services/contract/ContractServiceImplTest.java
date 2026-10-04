package com.promaty.rrhh.services.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.contract.ContractDetailDto;
import com.promaty.rrhh.entity.Company;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.ContractType;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.entity.JobTitle;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Site;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;

@ExtendWith(MockitoExtension.class)
class ContractServiceImplTest {

	@Mock
	private ContractRepository contractRepository;

	@InjectMocks
	private ContractServiceImpl service;

	@Test
	void getContractDetail_registroNoExiste_lanzaResourceNotFoundException() {
		when(contractRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getContractDetail(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getContractDetail_registroExiste_retornaDetalleMapeadoConSusRelaciones() {
		when(contractRepository.findById(1L)).thenReturn(Optional.of(contratoCompleto()));

		ContractDetailDto detalle = service.getContractDetail(1L);

		assertThat(detalle.getStaff().getIdentificationNumber()).isEqualTo("12345678-5");
		assertThat(detalle.getCompany().getName()).isEqualTo("Constructora Promaty S.A.");
		assertThat(detalle.getCostCenterCode()).isEqualTo("00824");
		assertThat(detalle.getStatus().getCode()).isEqualTo("PENDING_APPROVAL");
	}

	private Contract contratoCompleto() {
		Staff staff = new Staff();
		staff.setId(1L);
		staff.setIdentificationType(IdentificationType.RUT);
		staff.setIdentificationNumber("12345678-5");
		staff.setFirstName("Juan");
		staff.setPaternalLastName("Perez");
		staff.setMaternalLastName("Soto");

		Company company = new Company();
		company.setId(1L);
		company.setName("Constructora Promaty S.A.");

		ContractType contractType = new ContractType();
		contractType.setId(1L);
		contractType.setName("Contrato indefinido");

		JobTitle jobTitle = new JobTitle();
		jobTitle.setId(1L);
		jobTitle.setName("Albañiles");

		Site site = new Site();
		site.setId(1L);
		site.setName("Casa matriz");

		Project project = new Project();
		project.setId(1L);
		project.setCostCenterCode("00824");

		PlatformStatus status = new PlatformStatus();
		status.setId(1L);
		status.setName("Pendiente de aprobación");
		status.setCode("PENDING_APPROVAL");

		Contract contract = new Contract();
		contract.setId(1L);
		contract.setStaff(staff);
		contract.setCompany(company);
		contract.setContractType(contractType);
		contract.setJobTitle(jobTitle);
		contract.setSite(site);
		contract.setProject(project);
		contract.setStartDate(LocalDate.of(2026, 1, 1));
		contract.setStatus(status);
		return contract;
	}
}
