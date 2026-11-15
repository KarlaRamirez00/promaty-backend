package com.promaty.rrhh.services.shared.attachment;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.entity.UploadedFile;

public record AttachmentPlan(List<Kept> kept, List<UploadedFile> removed, List<Added> added) {

	public record Kept(UploadedFile file, String name) {}

	public record Added(MultipartFile file, String name, String type, AllowedFileFormat format) {}
}
