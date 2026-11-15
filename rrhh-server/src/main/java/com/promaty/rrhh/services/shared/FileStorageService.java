package com.promaty.rrhh.services.shared;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String store(MultipartFile file, String prefix, String extension);

    Resource load(String storagePath);

    void delete(String storagePath);
}
