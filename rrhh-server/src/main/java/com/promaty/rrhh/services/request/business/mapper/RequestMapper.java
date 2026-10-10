package com.promaty.rrhh.services.request.business.mapper;

import java.util.List;

import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.dto.request.ApprovalSummaryDto;
import com.promaty.rrhh.dto.request.RequestDetailDto;
import com.promaty.rrhh.dto.request.RequestListDto;
import com.promaty.rrhh.entity.Approval;
import com.promaty.rrhh.entity.Request;

public final class RequestMapper {

	private RequestMapper() {
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
			null // actions depende del status de la fila y de los permisos del usuario: lo completa el service
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
