package com.promaty.rrhh.services.contract;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.contract.ContractCountersDto;
import com.promaty.rrhh.dto.contract.ContractDetailDto;
import com.promaty.rrhh.dto.contract.ContractFilterParams;
import com.promaty.rrhh.dto.contract.ContractListDto;
import com.promaty.rrhh.dto.file.FileAttachmentDto;
import com.promaty.rrhh.entity.AttachmentEntityType;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.ContractAttachmentType;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.entity.SettingKey;
import com.promaty.rrhh.entity.UploadedFile;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.repository.UploadedFileRepository;
import com.promaty.rrhh.services.file.business.mapper.UploadedFileMapper;
import com.promaty.rrhh.services.shared.FileStorageService;
import com.promaty.rrhh.services.contract.business.builder.ContractExpirationSpecification;
import com.promaty.rrhh.services.contract.business.builder.ContractQueryBuilder;
import com.promaty.rrhh.services.contract.business.mapper.ContractMapper;
import com.promaty.rrhh.services.setting.SettingProvider;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.shared.ProjectAccessSpecification;
import com.promaty.rrhh.services.shared.RequestApprovalActionsResolver;

@Service
public class ContractServiceImpl implements ContractService {

	private static final String NO_ENCONTRADO = "Contrato no encontrado.";
	private static final String CODE_EXPIRED = "EXPIRED";
	private static final String PERMISO_APROBAR = "contract.approve";
	private static final String PERMISO_VALIDAR = "contract.validate";
	private static final List<String> TIPOS_PERMITIDOS = List.of(
		"application/pdf", "image/jpeg", "image/png",
		"application/msword",
		"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
		"application/vnd.ms-excel",
		"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
	);
	private static final List<ContractAttachmentType> TIPOS_REQUERIDOS = Arrays.asList(ContractAttachmentType.values());

	private final ContractRepository contractRepository;
	private final RequestRepository requestRepository;
	private final RequestApprovalActionsResolver actionsResolver;
	private final SettingProvider settingProvider;
	private final UploadedFileRepository uploadedFileRepository;
	private final FileStorageService fileStorageService;

	public ContractServiceImpl(
		ContractRepository contractRepository,
		RequestRepository requestRepository,
		RequestApprovalActionsResolver actionsResolver,
		SettingProvider settingProvider,
		UploadedFileRepository uploadedFileRepository,
		FileStorageService fileStorageService
	) {
		this.contractRepository = contractRepository;
		this.requestRepository = requestRepository;
		this.actionsResolver = actionsResolver;
		this.settingProvider = settingProvider;
		this.uploadedFileRepository = uploadedFileRepository;
		this.fileStorageService = fileStorageService;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ContractListDto> listContracts(ContractFilterParams filters, Pageable pageable) {
		Specification<Contract> especificacion = withExpiringSoonFilter(
			ContractQueryBuilder.fromFilters(filters).and(ProjectAccessSpecification.onProject()),
			filters
		);
		Page<ContractListDto> pagina = contractRepository.findAll(especificacion, pageable).map(ContractMapper::toListDto);
		Map<Long, Request> requestsPorContrato = buscarRequestsPorContrato(pagina.map(ContractListDto::getId).getContent());
		Set<String> autoridades = CurrentUserAuthorities.get();
		return pagina.map(dto -> conRequestYAcciones(dto, requestsPorContrato.get(dto.getId()), autoridades));
	}

	@Override
	@Transactional(readOnly = true)
	public ContractDetailDto getContractDetail(Long id) {
		Contract contract = buscarPorId(id);
		ContractDetailDto dto = ContractMapper.toDetailDto(contract);
		Request request = requestRepository.findByEntityTypeAndEntityId(RequestEntityType.CONTRACT, id).orElse(null);
		dto.setRequestId(request == null ? null : request.getId());
		String statusCode = request == null ? null : request.getStatus().getCode();
		dto.setActions(actionsResolver.resolve(statusCode, CurrentUserAuthorities.get(), PERMISO_APROBAR, PERMISO_VALIDAR));
		List<FileAttachmentDto> attachments = loadAttachments(id);
		dto.setAttachments(attachments);
		dto.setMissingAttachmentTypes(resolveMissingTypes(attachments));
		return dto;
	}

	@Override
	@Transactional
	public FileAttachmentDto uploadAttachment(Long contractId, ContractAttachmentType type, MultipartFile file) {
		buscarPorId(contractId);
		if (file == null || file.isEmpty()) {
			throw new BusinessValidationException("Archivo inválido.", Map.of("file", "El archivo está vacío."));
		}
		if (!TIPOS_PERMITIDOS.contains(file.getContentType())) {
			throw new BusinessValidationException(
				"Tipo de archivo no permitido.",
				Map.of("file", "Se aceptan: PDF, JPG, PNG, DOC, DOCX, XLS, XLSX.")
			);
		}
		uploadedFileRepository.findByEntityTypeAndEntityIdAndAttachmentType(
			AttachmentEntityType.CONTRACT, contractId, type.name()
		).ifPresent(existing -> {
			fileStorageService.delete(existing.getStoragePath());
			uploadedFileRepository.delete(existing);
		});
		String storagePath = fileStorageService.store(file, "contract/" + contractId);
		UploadedFile uploaded = new UploadedFile();
		uploaded.setMediaFileId(UUID.randomUUID().toString());
		uploaded.setEntityType(AttachmentEntityType.CONTRACT);
		uploaded.setEntityId(contractId);
		uploaded.setFileName(file.getOriginalFilename());
		uploaded.setStoragePath(storagePath);
		uploaded.setContentType(file.getContentType());
		uploaded.setFileSize(file.getSize());
		uploaded.setAttachmentType(type.name());
		return UploadedFileMapper.toDto(uploadedFileRepository.save(uploaded));
	}

	@Override
	@Transactional
	public void deleteAttachment(Long contractId, Long attachmentId) {
		buscarPorId(contractId);
		UploadedFile archivo = uploadedFileRepository.findById(attachmentId)
			.filter(f -> f.getEntityType() == AttachmentEntityType.CONTRACT && f.getEntityId().equals(contractId))
			.orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado."));
		fileStorageService.delete(archivo.getStoragePath());
		uploadedFileRepository.delete(archivo);
	}

	@Override
	@Transactional(readOnly = true)
	public ContractCountersDto getCounters() {
		int expiringSoonDays = settingProvider.getInt(SettingKey.EXPIRING_CONTRACT_DAYS);
		return new ContractCountersDto(contarVencidos(), contarPorVencer(expiringSoonDays), expiringSoonDays);
	}

	private long contarVencidos() {
		Specification<Contract> especificacion = ProjectAccessSpecification.<Contract>onProject()
			.and((root, query, cb) -> cb.equal(root.get("status").get("code"), CODE_EXPIRED));
		return contractRepository.count(especificacion);
	}

	private long contarPorVencer(int expiringSoonDays) {
		Specification<Contract> especificacion = ProjectAccessSpecification.<Contract>onProject()
			.and(ContractExpirationSpecification.expiringSoon(LocalDate.now(), expiringSoonDays));
		return contractRepository.count(especificacion);
	}

	private Specification<Contract> withExpiringSoonFilter(Specification<Contract> base, ContractFilterParams filters) {
		if (!Boolean.TRUE.equals(filters.getExpiringSoon())) {
			return base;
		}
		int expiringSoonDays = settingProvider.getInt(SettingKey.EXPIRING_CONTRACT_DAYS);
		return base.and(ContractExpirationSpecification.expiringSoon(LocalDate.now(), expiringSoonDays));
	}

	private Contract buscarPorId(Long id) {
		return contractRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}

	private List<FileAttachmentDto> loadAttachments(Long contractId) {
		return uploadedFileRepository.findByEntityTypeAndEntityId(AttachmentEntityType.CONTRACT, contractId)
			.stream()
			.map(UploadedFileMapper::toDto)
			.toList();
	}

	private List<String> resolveMissingTypes(List<FileAttachmentDto> attachments) {
		Set<String> present = attachments.stream()
			.map(FileAttachmentDto::getAttachmentType)
			.collect(Collectors.toSet());
		return TIPOS_REQUERIDOS.stream()
			.map(ContractAttachmentType::name)
			.filter(type -> !present.contains(type))
			.toList();
	}

	private Map<Long, Request> buscarRequestsPorContrato(List<Long> contractIds) {
		if (contractIds.isEmpty()) {
			return Map.of();
		}
		return requestRepository.findByEntityTypeAndEntityIdIn(RequestEntityType.CONTRACT, contractIds).stream()
			.collect(Collectors.toMap(Request::getEntityId, Function.identity()));
	}

	private ContractListDto conRequestYAcciones(ContractListDto dto, Request request, Set<String> autoridades) {
		dto.setRequestId(request == null ? null : request.getId());
		String statusCode = request == null ? null : request.getStatus().getCode();
		dto.setActions(actionsResolver.resolve(statusCode, autoridades, PERMISO_APROBAR, PERMISO_VALIDAR));
		return dto;
	}
}
