package com.promaty.rrhh.services.annex.business.change;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.annex.UpdateAnnexDto;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.ContractType;
import com.promaty.rrhh.repository.ContractTypeRepository;

@ExtendWith(MockitoExtension.class)
class ConversionToIndefiniteRuleTest {

	@Mock
	private ContractTypeRepository contractTypeRepository;

	@InjectMocks
	private ConversionToIndefiniteRule rule;

	@Test
	void validate_contratoYaIndefinido_rechazaElTipoDeAnexo() {
		Map<String, String> errores = new LinkedHashMap<>();

		rule.validate(new UpdateAnnexDto(), contratoDe(tipo("I"), null), errores);

		assertThat(errores).containsEntry("annexTypeId", "El contrato ya es indefinido.");
	}

	@Test
	void validate_contratoPorObraOFaena_noAgregaErrores() {
		Map<String, String> errores = new LinkedHashMap<>();

		rule.validate(new UpdateAnnexDto(), contratoDe(tipo("O"), null), errores);

		assertThat(errores).isEmpty();
	}

	@Test
	void apply_pasaElContratoAIndefinidoSinFechaDeTerminoYGuardaLoAnterior() {
		ContractType plazoFijo = tipo("F");
		ContractType indefinido = tipo("I");
		Contract contract = contratoDe(plazoFijo, LocalDate.of(2026, 12, 31));
		when(contractTypeRepository.findByCode("I")).thenReturn(Optional.of(indefinido));
		Annex annex = new Annex();

		rule.apply(annex, contract);

		assertThat(contract.getContractType()).isEqualTo(indefinido);
		assertThat(contract.getEndDate()).isNull();
		assertThat(annex.getPreviousContractType()).isEqualTo(plazoFijo);
		assertThat(annex.getPreviousEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
	}

	private ContractType tipo(String code) {
		ContractType type = new ContractType();
		type.setCode(code);
		return type;
	}

	private Contract contratoDe(ContractType type, LocalDate endDate) {
		Contract contract = new Contract();
		contract.setContractType(type);
		contract.setEndDate(endDate);
		return contract;
	}
}
