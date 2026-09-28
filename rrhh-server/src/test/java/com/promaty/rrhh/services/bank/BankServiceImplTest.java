package com.promaty.rrhh.services.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.bank.BankOptionDto;
import com.promaty.rrhh.entity.Bank;
import com.promaty.rrhh.repository.BankRepository;

@ExtendWith(MockitoExtension.class)
class BankServiceImplTest {

	@Mock
	private BankRepository bankRepository;

	@InjectMocks
	private BankServiceImpl service;

	@Test
	void listOptions_marcaSupportsRutAccountSoloParaBancoEstado() {
		Bank bancoEstado = new Bank();
		bancoEstado.setId(1L);
		bancoEstado.setName("BancoEstado");
		bancoEstado.setCode("BANCO_ESTADO");
		Bank santander = new Bank();
		santander.setId(2L);
		santander.setName("Banco Santander");
		santander.setCode("SANTANDER");
		when(bankRepository.findByActiveTrueOrderByName()).thenReturn(List.of(bancoEstado, santander));

		List<BankOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(2);
		assertThat(opciones.get(0).isSupportsRutAccount()).isTrue();
		assertThat(opciones.get(1).isSupportsRutAccount()).isFalse();
	}
}
