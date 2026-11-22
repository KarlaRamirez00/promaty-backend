package com.promaty.rrhh.services.request.business.mapper;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.dto.request.ApprovalSummaryDto;
import com.promaty.rrhh.dto.request.RequestDetailDto;
import com.promaty.rrhh.dto.request.RequestListDto;
import com.promaty.rrhh.dto.request.RequesterOptionDto;
import com.promaty.rrhh.entity.Approval;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.repository.projection.RequesterOptionProjection;

public final class RequestMapper {

	private RequestMapper() {
	}

	public static List<RequesterOptionDto> toRequesterOptions(List<RequesterOptionProjection> rows) {
		Map<Long, String> nameById = new LinkedHashMap<>();
		rows.forEach(row -> nameById.putIfAbsent(row.getId(), row.getName()));
		return nameById.entrySet().stream()
			.map(entry -> new RequesterOptionDto(entry.getKey(), entry.getValue()))
			.sorted(Comparator.comparing(RequesterOptionDto::getName, String.CASE_INSENSITIVE_ORDER))
			.toList();
	}

	public static RequestListDto toListDto(Request request) {
		return new RequestListDto(
			request.getId(),
			request.getEntityType(),
			request.getAction(),
			RequestTypeLabelResolver.resolve(request.getEntityType(), request.getAction()),
			request.getEntityId(),
			request.getProject().getName(),
			request.getProject().getCostCenterCode(),
			request.getRequesterUserId(),
			request.getCreatedBy(),
			null, // staffIdentificationType: resuelto en lote por el service a partir del Contract
			null, // staffIdentificationNumber: idem
			null, // staffFirstName: idem
			null, // staffPaternalLastName: idem
			null, // staffMaternalLastName: idem
			null, // staffPosition: idem
			new PlatformStatusOptionDto(request.getStatus().getId(), request.getStatus().getCode(), request.getStatus().getName()),
			request.getCreatedAt(),
			request.getUpdatedAt(),
			request.getCreatedBy(),
			request.getUpdatedBy(),
			null // actions depende del status de la fila y de los permisos del usuario: lo completa el service
		);
	}

	public static RequestDetailDto toDetailDto(Request request, List<Approval> approvals) {
		return new RequestDetailDto(
			request.getId(),
			request.getEntityType(),
			request.getAction(),
			RequestTypeLabelResolver.resolve(request.getEntityType(), request.getAction()),
			request.getEntityId(),
			request.getPendingData(),
			null, // contractPendingData: lo completa el service (necesita repositorios para resolver nombres)
			null, // terminationPendingData: idem
			request.getProject().getId(),
			request.getProject().getName(),
			request.getProject().getCostCenterCode(),
			request.getRequesterUserId(),
			request.getCreatedBy(),
			new PlatformStatusOptionDto(request.getStatus().getId(), request.getStatus().getCode(), request.getStatus().getName()),
			approvals.stream().map(RequestMapper::toApprovalSummary).toList(),
			request.getCreatedAt(),
			request.getUpdatedAt(),
			request.getCreatedBy(),
			request.getUpdatedBy(),
			null, // actions depende del status de la fila y de los permisos del usuario: lo completa el service
			request.getSyncErrorMessage()
		);
	}

	private static ApprovalSummaryDto toApprovalSummary(Approval approval) {
		return new ApprovalSummaryDto(
			approval.getId(),
			approval.getLevel(),
			approval.getDecision(),
			approval.getRejectionReason() != null ? approval.getRejectionReason().getId() : null,
			approval.getRejectionReason() != null ? approval.getRejectionReason().getName() : null,
			approval.getComment(),
			approval.getApproverUserId(),
			approval.getCreatedBy(),
			approval.getCreatedAt()
		);
	}
}
