package com.promaty.rrhh.services.request.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.promaty.rrhh.dto.request.AnnexPendingDataDto;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexType;
import com.promaty.rrhh.entity.AnnexTypeCode;
import com.promaty.rrhh.entity.ApprovalDecision;
import com.promaty.rrhh.entity.ApprovalLevel;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.AnnexRepository;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.services.annex.business.builder.AnnexRelationsResolver;
import com.promaty.rrhh.services.annex.business.builder.CreateAnnexBuilder;
import com.promaty.rrhh.services.annex.business.change.AnnexChangeRuleRegistry;
import com.promaty.rrhh.services.annex.business.change.SalaryIncreaseRule;
import com.promaty.rrhh.services.annex.business.validation.AnnexValidation;
import com.promaty.rrhh.services.rexplus.RexPlusClient;
import com.promaty.rrhh.services.rexplus.RexSyncResult;

@ExtendWith(MockitoExtension.class)
class AnnexRequestHandlerTest {

	private static final Long ANNEX_ID = 20L;
	private static final String PENDING_DATA = """
		{"staffId":1,"annexTypeId":3,"effectiveDate":"2026-11-20","description":"Aumento","newBaseSalary":950000}
		""";

	@Mock
	private AnnexRepository annexRepository;
	@Mock
	private ContractRepository contractRepository;
	@Mock
	private CreateAnnexBuilder createAnnexBuilder;
	@Mock
	private AnnexRelationsResolver relationsResolver;
	@Mock
	private AnnexValidation annexValidation;
	@Mock
	private RexPlusClient rexPlusClient;

	private AnnexRequestHandler handler;

	@BeforeEach
	void setUp() {
		handler = new AnnexRequestHandler(
			new ObjectMapper().registerModule(new JavaTimeModule()),
			annexRepository, contractRepository, createAnnexBuilder, relationsResolver, annexValidation,
			new AnnexChangeRuleRegistry(List.of(new SalaryIncreaseRule())), rexPlusClient
		);
	}

	@Test
	void create_construyeElAnexoDesdeElPendingDataYRetornaSuId() {
		Project project = new Project();
		Request request = new Request();
		request.setProject(project);
		request.setPendingData(PENDING_DATA);
		Annex construido = new Annex();
		when(createAnnexBuilder.build(any(AnnexPendingDataDto.class), eq(project))).thenReturn(construido);
		Annex guardado = new Annex();
		guardado.setId(ANNEX_ID);
		when(annexRepository.save(construido)).thenReturn(guardado);

		assertThat(handler.create(request)).isEqualTo(ANNEX_ID);
	}

	@Test
	void applyDecision_conRechazo_dejaElAnexoRechazadoSinValidarNiTocarElContrato() {
		Annex annex = anexoPendiente("PENDING_APPROVAL");
		PlatformStatus rechazado = estado("REJECTED");
		when(relationsResolver.resolveStatus("REJECTED")).thenReturn(rechazado);

		handler.applyDecision(requestDelAnexo(), ApprovalLevel.PROJECT_MANAGER, ApprovalDecision.REJECTED);

		assertThat(annex.getStatus()).isEqualTo(rechazado);
		verifyNoInteractions(annexValidation, contractRepository);
	}

	@Test
	void applyDecision_aprobacionDeJefatura_validaYPasaAPendienteDeValidacion() {
		Annex annex = anexoPendiente("PENDING_APPROVAL");
		PlatformStatus pendienteValidacion = estado("PENDING_VALIDATION");
		when(relationsResolver.resolveStatus("PENDING_VALIDATION")).thenReturn(pendienteValidacion);

		handler.applyDecision(requestDelAnexo(), ApprovalLevel.PROJECT_MANAGER, ApprovalDecision.APPROVED);

		verify(annexValidation).validateApproval(annex);
		assertThat(annex.getStatus()).isEqualTo(pendienteValidacion);
		verify(rexPlusClient, never()).syncAnnex(any());
	}

	@Test
	void applyDecision_aprobarCuandoYaNoEsValido_lanzaErrorSinCambiarEstado() {
		Annex annex = anexoPendiente("PENDING_APPROVAL");
		doThrow(new BusinessValidationException("x", Map.of("staffId", "bloqueado"))).when(annexValidation).validateApproval(annex);

		assertThatThrownBy(() -> handler.applyDecision(requestDelAnexo(), ApprovalLevel.PROJECT_MANAGER, ApprovalDecision.APPROVED))
			.isInstanceOf(BusinessValidationException.class);

		assertThat(annex.getStatus().getCode()).isEqualTo("PENDING_APPROVAL");
		verify(annexRepository, never()).save(any());
	}

	@Test
	void applyDecision_validacionRrhhConSincronizacionExitosa_aplicaElCambioAlContrato() {
		Annex annex = anexoPendiente("PENDING_VALIDATION");
		when(rexPlusClient.syncAnnex(annex)).thenReturn(RexSyncResult.success("REX-ANX-1"));
		PlatformStatus aprobado = estado("APPROVED");
		when(relationsResolver.resolveStatus("APPROVED")).thenReturn(aprobado);

		handler.applyDecision(requestDelAnexo(), ApprovalLevel.HR, ApprovalDecision.APPROVED);

		assertThat(annex.getContract().getBaseSalary()).isEqualTo(BigDecimal.valueOf(950000));
		assertThat(annex.getPreviousBaseSalary()).isEqualTo(BigDecimal.valueOf(850000));
		assertThat(annex.getRexAnnexNumber()).isEqualTo("REX-ANX-1");
		assertThat(annex.getStatus()).isEqualTo(aprobado);
		verify(contractRepository).save(annex.getContract());
	}

	@Test
	void applyDecision_validacionRrhhConSincronizacionFallida_dejaSyncErrorSinTocarElContrato() {
		Annex annex = anexoPendiente("PENDING_VALIDATION");
		when(rexPlusClient.syncAnnex(annex)).thenReturn(RexSyncResult.failure("Rex+ no respondió."));
		PlatformStatus syncError = estado("SYNC_ERROR");
		when(relationsResolver.resolveStatus("SYNC_ERROR")).thenReturn(syncError);

		Request request = requestDelAnexo();

		handler.applyDecision(request, ApprovalLevel.HR, ApprovalDecision.APPROVED);

		assertThat(annex.getStatus()).isEqualTo(syncError);
		assertThat(annex.getContract().getBaseSalary()).isEqualTo(BigDecimal.valueOf(850000));
		assertThat(request.getSyncErrorMessage()).isEqualTo("Rex+ no respondió.");
		verify(contractRepository, never()).save(any());
	}

	@Test
	void retrySync_conSincronizacionExitosa_aplicaElCambioAlContratoYBorraElMotivo() {
		Annex annex = anexoPendiente("SYNC_ERROR");
		when(rexPlusClient.syncAnnex(annex)).thenReturn(RexSyncResult.success("REX-ANX-2"));
		when(relationsResolver.resolveStatus("APPROVED")).thenReturn(estado("APPROVED"));
		Request request = requestDelAnexo();
		request.recordSyncFailure("Rex+ no respondió.");

		handler.retrySync(request);

		assertThat(annex.getStatus().getCode()).isEqualTo("APPROVED");
		assertThat(annex.getContract().getBaseSalary()).isEqualTo(BigDecimal.valueOf(950000));
		assertThat(request.getSyncErrorMessage()).isNull();
	}

	@Test
	void isInSyncError_conAnexoEnSyncError_retornaTrue() {
		anexoPendiente("SYNC_ERROR");

		assertThat(handler.isInSyncError(ANNEX_ID)).isTrue();
	}

	private Annex anexoPendiente(String statusCode) {
		AnnexType type = new AnnexType();
		type.setCode(AnnexTypeCode.SALARY_INCREASE);
		Contract contract = new Contract();
		contract.setBaseSalary(BigDecimal.valueOf(850000));
		Annex annex = new Annex();
		annex.setId(ANNEX_ID);
		annex.setAnnexType(type);
		annex.setContract(contract);
		annex.setNewBaseSalary(BigDecimal.valueOf(950000));
		annex.setStatus(estado(statusCode));
		when(annexRepository.findById(ANNEX_ID)).thenReturn(Optional.of(annex));
		return annex;
	}

	private Request requestDelAnexo() {
		Request request = new Request();
		request.setEntityId(ANNEX_ID);
		return request;
	}

	private PlatformStatus estado(String code) {
		PlatformStatus status = new PlatformStatus();
		status.setCode(code);
		return status;
	}
}
