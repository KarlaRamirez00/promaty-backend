package com.promaty.rrhh.dto.file;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileAttachmentDto {

    private Long id;
    private String name;
    private String fileType;
    private Long fileSize;
    private String downloadUrl;
    private String mediaFileId;
    private LocalDateTime updatedAt;
}
