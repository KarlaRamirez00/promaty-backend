package com.promaty.rrhh.services.annex.business.change;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.annex.UpdateAnnexDto;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexTypeCode;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.ContractType;
import com.promaty.rrhh.repository.AnnexRepository;

@ExtendWith(MockitoExtension.class)
class ContractExtensionRuleTest {

	private static final LocalDate TERMINO_ACTUAL = LocalDate.of(2026, 12, 31);

	@Mock
	private AnnexRepository annexRepository;

	@InjectMocks
	private ContractExtensionRule rule;

	@Test
	void validate_plazoFijoConFechaPosteriorYSinProrrogaPrevia_noAgregaErrores() {
		Map<String, String> errores = validar(contratoConTipo("F"), TERMINO_ACTUAL.plusMonths(3), false);

		assertThat(errores).isEmpty();
	}

	@Test
	void validate_contratoIndefinido_rechazaElTipoDeAnexo() {
		Map<String, String> errores = new LinkedHashMap<>();

		rule.validate(cambioCon(TERMINO_ACTUAL.plusMonths(3)), contratoConTipo("I"), errores);

		assertThat(errores).containsKey("annexTypeId");
	}

	@Test
	void validate_conProrrogaYaAprobada_rechazaPorqueLaSegundaLoConvierteEnIndefinido() {
		Map<String, String> errores = validar(contratoConTipo("F"), TERMINO_ACTUAL.plusMonths(3), true);

		assertThat(errores.get("annexTypeId")).contains("paso a indefinido");
	}

	@Test
	void validate_fechaNoPosteriorAlTerminoActual_rechazaLaNuevaFecha() {
		Map<String, String> errores = validar(contratoConTipo("F"), TERMINO_ACTUAL, false);

		assertThat(errores).containsKey("newEndDate");
	}

	@Test
	void validate_sinNuevaFecha_laExigeComoObligatoria() {
		Map<String, String> errores = validar(contratoConTipo("F"), null, false);

		assertThat(errores).containsEntry("newEndDate", "La nueva fecha de término es obligatoria.");
	}

	@Test
	void apply_guardaElTerminoAnteriorYMueveElDelContrato() {
		Contract contract = contratoConTipo("F");
		Annex annex = new Annex();
		annex.setNewEndDate(TERMINO_ACTUAL.plusMonths(6));

		rule.apply(annex, contract);

		assertThat(annex.getPreviousEndDate()).isEqualTo(TERMINO_ACTUAL);
		assertThat(contract.getEndDate()).isEqualTo(TERMINO_ACTUAL.plusMonths(6));
	}

	private Map<String, String> validar(Contract contract, LocalDate nuevaFecha, boolean yaTuvoProrroga) {
		when(annexRepository.existsByContract_IdAndAnnexType_CodeAndStatus_Code(
			any(), eq(AnnexTypeCode.CONTRACT_EXTENSION), eq("APPROVED")
		)).thenReturn(yaTuvoProrroga);
		Map<String, String> errores = new LinkedHashMap<>();
		rule.validate(cambioCon(nuevaFecha), contract, errores);
		return errores;
	}

	private UpdateAnnexDto cambioCon(LocalDate nuevaFecha) {
		UpdateAnnexDto dto = new UpdateAnnexDto();
		dto.setNewEndDate(nuevaFecha);
		return dto;
	}

	private Contract contratoConTipo(String code) {
		ContractType type = new ContractType();
		type.setCode(code);
		Contract contract = new Contract();
		contract.setId(1L);
		contract.setContractType(type);
		contract.setEndDate(TERMINO_ACTUAL);
		return contract;
	}
}
