package com.promaty.rrhh.services.shared;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.exception.FileStorageException;
import com.promaty.rrhh.exception.ResourceNotFoundException;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Pattern SAFE_EXTENSION = Pattern.compile("[a-z0-9]{1,10}");
    private static final String DEFAULT_EXTENSION = "bin";

    private final Path uploadDir;

    public LocalFileStorageService(@Value("${file.upload-dir:/uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file, String prefix, String extension) {
        try {
            Path targetDir = uploadDir.resolve(prefix);
            Files.createDirectories(targetDir);
            String storedName = UUID.randomUUID() + "." + safeExtension(extension);
            Path target = targetDir.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return prefix + "/" + storedName;
        } catch (IOException e) {
            throw new FileStorageException("Error al guardar archivo en disco.", e);
        }
    }

    private String safeExtension(String extension) {
        String normalized = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        return SAFE_EXTENSION.matcher(normalized).matches() ? normalized : DEFAULT_EXTENSION;
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
            throw new FileStorageException("Error al eliminar archivo del disco.", e);
        }
    }
}
