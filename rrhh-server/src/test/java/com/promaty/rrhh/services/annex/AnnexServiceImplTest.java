package com.promaty.rrhh.services.annex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.rrhh.dto.annex.AnnexDetailDto;
import com.promaty.rrhh.dto.annex.UpdateAnnexDto;
import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexType;
import com.promaty.rrhh.entity.AnnexTypeCode;
import com.promaty.rrhh.entity.AttachmentEntityType;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.AnnexRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.services.annex.business.builder.UpdateAnnexBuilder;
import com.promaty.rrhh.services.annex.business.validation.AnnexValidation;
import com.promaty.rrhh.services.shared.RequestApprovalActionsResolver;
import com.promaty.rrhh.services.shared.attachment.AttachmentService;

@ExtendWith(MockitoExtension.class)
class AnnexServiceImplTest {

	@Mock
	private AnnexRepository annexRepository;
	@Mock
	private RequestRepository requestRepository;
	@Mock
	private AnnexValidation annexValidation;
	@Mock
	private UpdateAnnexBuilder updateAnnexBuilder;
	@Spy
	private RequestApprovalActionsResolver actionsResolver = new RequestApprovalActionsResolver();
	@Mock
	private AttachmentService attachmentService;

	@InjectMocks
	private AnnexServiceImpl service;

	@AfterEach
	void limpiarContextoDeSeguridad() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getAnnexDetail_registroNoExiste_lanzaResourceNotFound() {
		when(annexRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getAnnexDetail(1L)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getAnnexDetail_pendienteConPermisosDeAprobarYEditar_incluyeAmbasAcciones() {
		autenticarCon("annex.approve", "annex.update");
		when(annexRepository.findById(1L)).thenReturn(Optional.of(anexoConEstado("PENDING_APPROVAL")));
		when(requestRepository.findByEntityTypeAndEntityId(RequestEntityType.ANNEX, 1L))
			.thenReturn(Optional.of(requestConEstado("PENDING_APPROVAL")));
		when(attachmentService.list(AttachmentEntityType.ANNEX, 1L)).thenReturn(List.of());

		AnnexDetailDto detalle = service.getAnnexDetail(1L);

		assertThat(detalle.getRequestId()).isEqualTo(9L);
		assertThat(detalle.getActions()).containsExactly(Action.APPROVE, Action.UPDATE);
		assertThat(detalle.getAnnexType().getCode()).isEqualTo("SALARY_INCREASE");
	}

	@Test
	void updateAnnex_validaAntesDeAplicarYGuardar() {
		Annex annex = anexoConEstado("PENDING_APPROVAL");
		when(annexRepository.findById(1L)).thenReturn(Optional.of(annex));
		UpdateAnnexDto dto = new UpdateAnnexDto();

		service.updateAnnex(1L, dto);

		InOrder orden = inOrder(annexValidation, updateAnnexBuilder, annexRepository);
		orden.verify(annexValidation).validateUpdate(annex, dto);
		orden.verify(updateAnnexBuilder).apply(annex, dto);
		orden.verify(annexRepository).save(annex);
	}

	@Test
	void uploadAttachment_conAnexoNoEditable_noSubeElArchivo() {
		Annex annex = anexoConEstado("APPROVED");
		when(annexRepository.findById(1L)).thenReturn(Optional.of(annex));
		doThrow(new BusinessValidationException("x", Map.of("status", "no editable"))).when(annexValidation).validateEditable(annex);
		MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[] {1});

		assertThatThrownBy(() -> service.uploadAttachment(1L, file)).isInstanceOf(BusinessValidationException.class);

		verify(attachmentService, never()).replaceSingle(any(), any(), any());
	}

	private Annex anexoConEstado(String code) {
		Staff staff = new Staff();
		staff.setFirstName("Ana");
		staff.setPaternalLastName("Rojas");
		staff.setMaternalLastName("Vera");
		Project project = new Project();
		project.setCostCenterCode("00824");
		Contract contract = new Contract();
		contract.setId(10L);
		AnnexType type = new AnnexType();
		type.setId(3L);
		type.setName("Aumento de sueldo");
		type.setCode(AnnexTypeCode.SALARY_INCREASE);

		Annex annex = new Annex();
		annex.setId(1L);
		annex.setStaff(staff);
		annex.setProject(project);
		annex.setContract(contract);
		annex.setAnnexType(type);
		annex.setEffectiveDate(LocalDate.now());
		annex.setStatus(estado(code));
		return annex;
	}

	private Request requestConEstado(String code) {
		Request request = new Request();
		request.setId(9L);
		request.setStatus(estado(code));
		return request;
	}

	private PlatformStatus estado(String code) {
		PlatformStatus status = new PlatformStatus();
		status.setCode(code);
		return status;
	}

	private void autenticarCon(String... permisos) {
		List<SimpleGrantedAuthority> autoridades = java.util.Arrays.stream(permisos).map(SimpleGrantedAuthority::new).toList();
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("1", null, autoridades));
	}
}
