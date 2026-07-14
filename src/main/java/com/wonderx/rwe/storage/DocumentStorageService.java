package com.wonderx.rwe.storage;

import com.wonderx.rwe.config.StorageProperties;
import com.wonderx.rwe.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentStorageService {

    private final StorageProperties storageProperties;

    public String store(MultipartFile file, String folder) {
        try {
            Path dir = Paths.get(storageProperties.getUploadDir(), folder);
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target);
            return target.toString();
        } catch (IOException e) {
            throw new BusinessException("Failed to store file: " + e.getMessage());
        }
    }
}
