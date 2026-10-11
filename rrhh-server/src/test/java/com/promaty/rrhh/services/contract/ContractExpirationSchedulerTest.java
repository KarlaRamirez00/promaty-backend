package com.promaty.rrhh.services.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;

@ExtendWith(MockitoExtension.class)
class ContractExpirationSchedulerTest {

	@Mock
	private ContractRepository contractRepository;
	@Mock
	private PlatformStatusRepository platformStatusRepository;

	@InjectMocks
	private ContractExpirationScheduler scheduler;

	@Test
	void vencerContratosActivos_sinContratosVencidos_noConsultaElEstadoNiGuarda() {
		when(contractRepository.findByStatus_CodeAndEndDateBefore("ACTIVE", LocalDate.now())).thenReturn(List.of());

		scheduler.vencerContratosActivos();

		verify(platformStatusRepository, never()).findBySubModuleAndCode(any(), any());
		verify(contractRepository, never()).saveAll(any());
	}

	@Test
	void vencerContratosActivos_conContratosVencidos_losActualizaAEstadoExpired() {
		Contract contrato = new Contract();
		when(contractRepository.findByStatus_CodeAndEndDateBefore("ACTIVE", LocalDate.now()))
			.thenReturn(List.of(contrato));
		PlatformStatus expirado = new PlatformStatus();
		expirado.setCode("EXPIRED");
		when(platformStatusRepository.findBySubModuleAndCode("contract", "EXPIRED")).thenReturn(Optional.of(expirado));

		scheduler.vencerContratosActivos();

		assertThat(contrato.getStatus()).isEqualTo(expirado);
		verify(contractRepository).saveAll(List.of(contrato));
	}

	@Test
	void vencerContratosActivos_estadoExpiredNoSembrado_lanzaResourceNotFound() {
		when(contractRepository.findByStatus_CodeAndEndDateBefore("ACTIVE", LocalDate.now()))
			.thenReturn(List.of(new Contract()));
		when(platformStatusRepository.findBySubModuleAndCode("contract", "EXPIRED")).thenReturn(Optional.empty());

		assertThatThrownBy(scheduler::vencerContratosActivos).isInstanceOf(ResourceNotFoundException.class);
	}
}
