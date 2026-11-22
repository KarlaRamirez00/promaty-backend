package com.promaty.rrhh.services.contract.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.repository.StaffRepository;

@ExtendWith(MockitoExtension.class)
class AnnulContractBuilderTest {

	@Mock
	private ContractRepository contractRepository;
	@Mock
	private StaffRepository staffRepository;
	@Mock
	private RequestRepository requestRepository;
	@Mock
	private PlatformStatusRepository platformStatusRepository;

	@InjectMocks
	private AnnulContractBuilder builder;

	@Test
	void apply_guardaMotivoMarcaContratoYSolicitudComoAnuladosYRestauraAlColaborador() {
		PlatformStatus previousStaffStatus = status("staff", "UNLINKED");
		Contract contract = contratoActivo("ACTIVE", previousStaffStatus);
		Request request = new Request();
		request.setStatus(status("request", "APPROVED"));
		stubAnnulled("contract");
		stubAnnulled("request");
		when(requestRepository.findByEntityTypeAndEntityId(RequestEntityType.CONTRACT, 1L)).thenReturn(Optional.of(request));

		builder.apply(contract, "No se presentó a trabajar");

		assertThat(contract.getAnnulmentReason()).isEqualTo("No se presentó a trabajar");
		assertThat(contract.getStatus().getCode()).isEqualTo("ANNULLED");
		assertThat(request.getStatus().getCode()).isEqualTo("ANNULLED");
		assertThat(contract.getStaff().getStatus()).isEqualTo(previousStaffStatus);
		verify(contractRepository).save(contract);
		verify(staffRepository).save(contract.getStaff());
		verify(requestRepository).save(request);
	}

	@Test
	void apply_sinEstadoPrevioGuardado_dejaAlColaboradorEnPendingContract() {
		Contract contract = contratoActivo("ACTIVE", null);
		stubAnnulled("contract");
		when(platformStatusRepository.findBySubModuleAndCode("staff", "PENDING_CONTRACT"))
			.thenReturn(Optional.of(status("staff", "PENDING_CONTRACT")));
		when(requestRepository.findByEntityTypeAndEntityId(RequestEntityType.CONTRACT, 1L)).thenReturn(Optional.empty());

		builder.apply(contract, "motivo");

		assertThat(contract.getStaff().getStatus().getCode()).isEqualTo("PENDING_CONTRACT");
	}

	@Test
	void apply_colaboradorBloqueadoDespuesDeAprobar_noLoCambia() {
		Contract contract = contratoActivo("BLOCKED", status("staff", "PENDING_CONTRACT"));
		stubAnnulled("contract");
		when(requestRepository.findByEntityTypeAndEntityId(RequestEntityType.CONTRACT, 1L)).thenReturn(Optional.empty());

		builder.apply(contract, "motivo");

		assertThat(contract.getStaff().getStatus().getCode()).isEqualTo("BLOCKED");
		verify(staffRepository, never()).save(any());
	}

	private void stubAnnulled(String subModule) {
		when(platformStatusRepository.findBySubModuleAndCode(subModule, "ANNULLED"))
			.thenReturn(Optional.of(status(subModule, "ANNULLED")));
	}

	private Contract contratoActivo(String staffStatusCode, PlatformStatus staffStatusBefore) {
		Staff staff = new Staff();
		staff.setStatus(status("staff", staffStatusCode));

		Contract contract = new Contract();
		contract.setId(1L);
		contract.setStaff(staff);
		contract.setStatus(status("contract", "ACTIVE"));
		contract.setStaffStatusBeforeActivation(staffStatusBefore);
		return contract;
	}

	private PlatformStatus status(String subModule, String code) {
		PlatformStatus status = new PlatformStatus();
		status.setSubModule(subModule);
		status.setCode(code);
		return status;
	}
}
