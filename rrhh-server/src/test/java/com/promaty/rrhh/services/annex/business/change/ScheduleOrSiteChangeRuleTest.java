package com.promaty.rrhh.services.annex.business.change;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
import com.promaty.rrhh.entity.Site;
import com.promaty.rrhh.repository.SiteRepository;

@ExtendWith(MockitoExtension.class)
class ScheduleOrSiteChangeRuleTest {

	@Mock
	private SiteRepository siteRepository;

	@InjectMocks
	private ScheduleOrSiteChangeRule rule;

	@Test
	void validate_sinNingunCambio_exigeAlMenosUno() {
		Map<String, String> errores = validar(new UpdateAnnexDto());

		assertThat(errores).containsKey("newWeeklyWorkHours");
	}

	@Test
	void validate_horasDistintasDeLasActuales_noAgregaErrores() {
		UpdateAnnexDto dto = new UpdateAnnexDto();
		dto.setNewWeeklyWorkHours(40);

		assertThat(validar(dto)).isEmpty();
	}

	@Test
	void validate_valorIgualAlActual_marcaSoloEseCampo() {
		UpdateAnnexDto dto = new UpdateAnnexDto();
		dto.setNewWeeklyWorkHours(40);
		dto.setNewWorkDays(5);

		Map<String, String> errores = validar(dto);

		assertThat(errores).containsOnlyKeys("newWorkDays");
	}

	@Test
	void validate_sucursalInactiva_laRechaza() {
		UpdateAnnexDto dto = new UpdateAnnexDto();
		dto.setNewSiteId(2L);
		when(siteRepository.findById(2L)).thenReturn(Optional.of(sucursal(2L, false)));

		assertThat(validar(dto)).containsEntry("newSiteId", "La sucursal indicada no está activa.");
	}

	@Test
	void apply_soloCambiaLosCamposInformadosYGuardaSusValoresAnteriores() {
		Contract contract = contratoActual();
		Site nueva = sucursal(2L, true);
		Annex annex = new Annex();
		annex.setNewSite(nueva);

		rule.apply(annex, contract);

		assertThat(contract.getSite()).isEqualTo(nueva);
		assertThat(annex.getPreviousSite().getId()).isEqualTo(1L);
		assertThat(contract.getWeeklyWorkHours()).isEqualTo(45);
		assertThat(annex.getPreviousWeeklyWorkHours()).isNull();
	}

	private Map<String, String> validar(UpdateAnnexDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();
		rule.validate(dto, contratoActual(), errores);
		return errores;
	}

	private Contract contratoActual() {
		Contract contract = new Contract();
		contract.setWeeklyWorkHours(45);
		contract.setWorkDays(5);
		contract.setSite(sucursal(1L, true));
		return contract;
	}

	private Site sucursal(Long id, boolean active) {
		Site site = new Site();
		site.setId(id);
		site.setActive(active);
		return site;
	}
}
