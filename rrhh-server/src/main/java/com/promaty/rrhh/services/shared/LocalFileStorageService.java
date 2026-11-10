package com.promaty.rrhh.services.shared;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.exception.ResourceNotFoundException;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadDir;

    public LocalFileStorageService(@Value("${file.upload-dir:/uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file, String prefix) {
        try {
            Path targetDir = uploadDir.resolve(prefix);
            Files.createDirectories(targetDir);
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String storedName = UUID.randomUUID() + "_" + originalName;
            Path target = targetDir.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return prefix + "/" + storedName;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar archivo en disco", e);
        }
    }

    @Override
    public Resource load(String storagePath) {
        try {
            Path file = uploadDir.resolve(storagePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Archivo no encontrado");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Archivo no encontrado");
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path file = uploadDir.resolve(storagePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar archivo del disco", e);
        }
    }
}
