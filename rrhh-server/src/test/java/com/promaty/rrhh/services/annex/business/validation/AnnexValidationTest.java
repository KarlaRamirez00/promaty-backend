package com.promaty.rrhh.services.annex.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.annex.UpdateAnnexDto;
import com.promaty.rrhh.dto.request.AnnexPendingDataDto;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexType;
import com.promaty.rrhh.entity.AnnexTypeCode;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.AnnexRepository;
import com.promaty.rrhh.repository.AnnexTypeRepository;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.services.annex.business.change.AnnexChangeRuleRegistry;
import com.promaty.rrhh.services.annex.business.change.SalaryIncreaseRule;

@ExtendWith(MockitoExtension.class)
class AnnexValidationTest {

	private static final Long STAFF_ID = 1L;
	private static final Long ANNEX_TYPE_ID = 3L;
	private static final BigDecimal SUELDO_ACTUAL = BigDecimal.valueOf(850000);

	@Mock
	private StaffRepository staffRepository;
	@Mock
	private ContractRepository contractRepository;
	@Mock
	private AnnexTypeRepository annexTypeRepository;
	@Mock
	private AnnexRepository annexRepository;
	@Mock
	private TerminationRepository terminationRepository;

	private AnnexValidation validation;

	@BeforeEach
	void setUp() {
		validation = new AnnexValidation(
			staffRepository, contractRepository, annexTypeRepository, annexRepository, terminationRepository,
			new AnnexChangeRuleRegistry(List.of(new SalaryIncreaseRule()))
		);
	}

	@Test
	void collectCreateErrors_conDatosValidos_noAgregaErrores() {
		escenarioValido(estado("ACTIVE"));

		assertThat(errores(dtoValido())).isEmpty();
	}

	@Test
	void collectCreateErrors_conColaboradorBloqueado_rechazaElAnexo() {
		escenarioValido(estado("BLOCKED"));

		assertThat(errores(dtoValido()))
			.containsEntry("staffId", "El colaborador está bloqueado y no puede recibir anexos.");
	}

	@Test
	void collectCreateErrors_conOtroAnexoEnCurso_rechazaElAnexo() {
		escenarioValido(estado("ACTIVE"));
		when(annexRepository.existsByContract_IdAndStatus_CodeIn(eq(10L), any())).thenReturn(true);

		assertThat(errores(dtoValido()))
			.containsEntry("staffId", "Ya existe un anexo en curso para el contrato de este colaborador.");
	}

	@Test
	void collectCreateErrors_conFiniquitoEnCurso_rechazaElAnexo() {
		escenarioValido(estado("ACTIVE"));
		when(terminationRepository.findByContract_IdAndStatus_CodeIn(eq(10L), any())).thenReturn(List.of(new Termination()));

		assertThat(errores(dtoValido()))
			.containsEntry("staffId", "El contrato tiene un finiquito en curso: no admite anexos.");
	}

	@Test
	void collectCreateErrors_conCampoQueNoAplicaAlTipo_loRechaza() {
		escenarioValido(estado("ACTIVE"));
		AnnexPendingDataDto dto = dtoValido();
		dto.setNewEndDate(LocalDate.now().plusMonths(6));

		assertThat(errores(dto)).containsEntry("newEndDate", "No aplica al tipo de anexo seleccionado.");
	}

	@Test
	void collectCreateErrors_conFechaDeVigenciaFueraDelMesActualOSiguiente_laRechaza() {
		escenarioValido(estado("ACTIVE"));
		AnnexPendingDataDto dto = dtoValido();
		dto.setEffectiveDate(LocalDate.now().plusMonths(3));

		assertThat(errores(dto)).containsKey("effectiveDate");
	}

	@Test
	void collectCreateErrors_colaboradorSinContratoActivo_loRechaza() {
		when(staffRepository.findById(STAFF_ID)).thenReturn(Optional.of(staffConEstado(estado("ACTIVE"))));
		when(contractRepository.findByStaffIdAndStatus_Code(STAFF_ID, "ACTIVE")).thenReturn(Optional.empty());

		assertThat(errores(dtoValido())).containsEntry("staffId", "El colaborador no tiene un contrato activo.");
	}

	@Test
	void validateUpdate_conAnexoYaAprobadoPorJefatura_rechazaLaEdicion() {
		Annex annex = anexoConEstado("PENDING_VALIDATION");

		assertThatThrownBy(() -> validation.validateUpdate(annex, new UpdateAnnexDto()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields()).containsKey("status"));
	}

	@Test
	void validateApproval_conContratoQueYaNoEstaActivo_rechazaLaAprobacion() {
		Annex annex = anexoConEstado("PENDING_APPROVAL");
		annex.getContract().setStatus(estado("TERMINATED"));

		assertThatThrownBy(() -> validation.validateApproval(annex))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsEntry("status", "El contrato del anexo ya no está activo."));
	}

	@Test
	void validateApproval_conDatosVigentes_noLanzaExcepcion() {
		Annex annex = anexoConEstado("PENDING_APPROVAL");

		assertThatCode(() -> validation.validateApproval(annex)).doesNotThrowAnyException();
	}

	private Map<String, String> errores(AnnexPendingDataDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();
		validation.collectCreateErrors(dto, errores);
		return errores;
	}

	private void escenarioValido(PlatformStatus estadoStaff) {
		when(staffRepository.findById(STAFF_ID)).thenReturn(Optional.of(staffConEstado(estadoStaff)));
		when(contractRepository.findByStaffIdAndStatus_Code(STAFF_ID, "ACTIVE")).thenReturn(Optional.of(contratoActivo()));
		lenient().when(annexRepository.existsByContract_IdAndStatus_CodeIn(eq(10L), any())).thenReturn(false);
		lenient().when(terminationRepository.findByContract_IdAndStatus_CodeIn(eq(10L), any())).thenReturn(List.of());
		lenient().when(annexTypeRepository.findById(ANNEX_TYPE_ID)).thenReturn(Optional.of(tipoAumentoDeSueldo()));
	}

	private AnnexPendingDataDto dtoValido() {
		AnnexPendingDataDto dto = new AnnexPendingDataDto();
		dto.setStaffId(STAFF_ID);
		dto.setAnnexTypeId(ANNEX_TYPE_ID);
		dto.setEffectiveDate(LocalDate.now());
		dto.setDescription("Aumento acordado por desempeño.");
		dto.setNewBaseSalary(BigDecimal.valueOf(950000));
		return dto;
	}

	private Annex anexoConEstado(String code) {
		Annex annex = new Annex();
		annex.setStatus(estado(code));
		annex.setStaff(staffConEstado(estado("ACTIVE")));
		annex.setContract(contratoActivo());
		annex.setAnnexType(tipoAumentoDeSueldo());
		annex.setNewBaseSalary(BigDecimal.valueOf(950000));
		return annex;
	}

	private Contract contratoActivo() {
		Contract contract = new Contract();
		contract.setId(10L);
		contract.setStartDate(LocalDate.now().minusMonths(6));
		contract.setBaseSalary(SUELDO_ACTUAL);
		contract.setStatus(estado("ACTIVE"));
		return contract;
	}

	private AnnexType tipoAumentoDeSueldo() {
		AnnexType type = new AnnexType();
		type.setId(ANNEX_TYPE_ID);
		type.setCode(AnnexTypeCode.SALARY_INCREASE);
		type.setActive(true);
		return type;
	}

	private Staff staffConEstado(PlatformStatus estado) {
		Staff staff = new Staff();
		staff.setStatus(estado);
		return staff;
	}

	private PlatformStatus estado(String code) {
		PlatformStatus status = new PlatformStatus();
		status.setCode(code);
		return status;
	}
}
