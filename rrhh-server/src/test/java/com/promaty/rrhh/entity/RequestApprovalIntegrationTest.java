package com.promaty.rrhh.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.repository.ApprovalRepository;
import com.promaty.rrhh.repository.ClientRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.repository.ProjectSpecialtyRepository;
import com.promaty.rrhh.repository.ProjectTypeRepository;
import com.promaty.rrhh.repository.RequestRepository;

// @Transactional revierte todo al terminar, no persiste datos en la BD real.
@SpringBootTest
@Transactional
class RequestApprovalIntegrationTest {

	@Autowired
	private RequestRepository requestRepository;
	@Autowired
	private ApprovalRepository approvalRepository;
	@Autowired
	private PlatformStatusRepository platformStatusRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private ProjectTypeRepository projectTypeRepository;
	@Autowired
	private ProjectSpecialtyRepository projectSpecialtyRepository;
	@Autowired
	private ClientRepository clientRepository;

	@Test
	void requestSeed_traeLos4EstadosSembrados() {
		assertThat(platformStatusRepository.findBySubModuleAndCode("request", "PENDING_APPROVAL")).isPresent();
		assertThat(platformStatusRepository.findBySubModuleAndCode("request", "PENDING_VALIDATION")).isPresent();
		assertThat(platformStatusRepository.findBySubModuleAndCode("request", "APPROVED")).isPresent();
		assertThat(platformStatusRepository.findBySubModuleAndCode("request", "REJECTED")).isPresent();
	}

	@Test
	void mismoCodeEnDosSubmodulosDistintos_noViolaLaConstraint() {
		PlatformStatus enRequest = new PlatformStatus();
		enRequest.setName("Prueba");
		enRequest.setCode("REUSABLE_CODE_TEST");
		enRequest.setSubModule("request");
		enRequest.setActive(true);
		platformStatusRepository.saveAndFlush(enRequest);

		PlatformStatus enProject = new PlatformStatus();
		enProject.setName("Prueba");
		enProject.setCode("REUSABLE_CODE_TEST");
		enProject.setSubModule("project");
		enProject.setActive(true);

		assertThat(platformStatusRepository.saveAndFlush(enProject).getId()).isNotNull();
	}

	@Test
	void guardarRequestConApprovals_persisteRelacionesCorrectamente() {
		PlatformStatus initialStatus = platformStatusRepository
			.findBySubModuleAndCode("request", "PENDING_APPROVAL").orElseThrow();
		Project project = testProject();

		Request request = new Request();
		request.setEntityType(RequestEntityType.CONTRACT);
		request.setAction(RequestAction.CREATE);
		request.setPendingData("{\"staffId\":1}");
		request.setProject(project);
		request.setRequesterUserId(1L);
		request.setStatus(initialStatus);
		Request saved = requestRepository.save(request);

		Approval approval = new Approval();
		approval.setRequest(saved);
		approval.setApproverUserId(2L);
		approval.setLevel(ApprovalLevel.PROJECT_MANAGER);
		approval.setDecision(ApprovalDecision.APPROVED);
		approvalRepository.save(approval);

		assertThat(approvalRepository.findByRequest_IdOrderByCreatedAtAsc(saved.getId())).hasSize(1);
	}

	@Test
	void insertarPlatformStatusDuplicado_mismoSubmoduloYCode_violaLaConstraint() {
		PlatformStatus duplicate = new PlatformStatus();
		duplicate.setName("Duplicado de prueba");
		duplicate.setCode("PENDING_APPROVAL");
		duplicate.setSubModule("request");
		duplicate.setActive(true);

		assertThatThrownBy(() -> platformStatusRepository.saveAndFlush(duplicate))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	private Project testProject() {
		ProjectType type = new ProjectType();
		type.setName("Tipo de prueba " + System.nanoTime());
		type.setActive(true);
		projectTypeRepository.save(type);

		ProjectSpecialty specialty = new ProjectSpecialty();
		specialty.setName("Especialidad de prueba " + System.nanoTime());
		specialty.setActive(true);
		projectSpecialtyRepository.save(specialty);

		Client client = new Client();
		client.setName("Cliente de prueba " + System.nanoTime());
		client.setActive(true);
		clientRepository.save(client);

		PlatformStatus projectStatus = platformStatusRepository
			.findBySubModuleAndCode("project", "PLANNED").orElseThrow();

		Project project = new Project();
		project.setName("Proyecto de prueba");
		project.setCostCenterCode("CC" + System.nanoTime());
		project.setType(type);
		project.setSpecialty(specialty);
		project.setClient(client);
		project.setStatus(projectStatus);
		project.setStartDate(LocalDate.now());
		return projectRepository.save(project);
	}
}
