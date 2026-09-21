package com.arose.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path baseStoragePath;

    public LocalStorageService(
            @Value("${storage.local.base-path:uploads}") String basePath
    ) {
        this.baseStoragePath = Paths.get(basePath)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.baseStoragePath);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not initialize storage directory",
                    e
            );
        }
    }

    @Override
    public String store(MultipartFile file, String folder) {

        // 1. Check that file exists
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File cannot be empty"
            );
        }

        // 2. Get file information
        String contentType = file.getContentType();

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null
                        ? ""
                        : file.getOriginalFilename()
        );

        // 3. Validate content type
        boolean validContentType =
                contentType != null &&
                        contentType.startsWith("image/");

        // 4. Validate file extension
        boolean validExtension =
                originalFilename.toLowerCase().matches(
                        ".*\\.(jpg|jpeg|png|webp)$"
                );

        // 5. Reject invalid files
        if (!validContentType && !validExtension) {
            throw new IllegalArgumentException(
                    "Only JPG, JPEG, PNG, and WEBP images are allowed"
            );
        }

        // 6. Determine extension
        String extension = getExtension(
                originalFilename,
                contentType
        );

        // 7. Generate unique filename
        String generatedFilename =
                UUID.randomUUID() + extension;

        // 8. Create requested folder safely
        Path folderPath = baseStoragePath
                .resolve(folder)
                .normalize();

        if (!folderPath.startsWith(baseStoragePath)) {
            throw new IllegalArgumentException(
                    "Invalid storage folder"
            );
        }

        try {

            // 9. Create folder if it doesn't exist
            Files.createDirectories(folderPath);

            // 10. Create target file path
            Path targetPath = folderPath
                    .resolve(generatedFilename)
                    .normalize();

            // 11. Prevent path traversal
            if (!targetPath.startsWith(folderPath)) {
                throw new IllegalArgumentException(
                        "Invalid file path"
                );
            }

            // 12. Save file
            Files.copy(
                    file.getInputStream(),
                    targetPath
            );

            // 13. Return storage key
            return folder + "/" + generatedFilename;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not store file",
                    e
            );
        }
    }

    @Override
    public Resource load(String storageKey) {

        Path filePath = baseStoragePath
                .resolve(storageKey)
                .normalize();

        // Prevent path traversal
        if (!filePath.startsWith(baseStoragePath)) {
            throw new IllegalArgumentException(
                    "Invalid storage key"
            );
        }

        try {

            Resource resource =
                    new UrlResource(
                            filePath.toUri()
                    );

            if (!resource.exists()) {
                throw new RuntimeException(
                        "File not found: " + storageKey
                );
            }

            return resource;

        } catch (MalformedURLException e) {

            throw new RuntimeException(
                    "Could not load file",
                    e
            );
        }
    }

    @Override
    public void delete(String storageKey) {

        Path filePath = baseStoragePath
                .resolve(storageKey)
                .normalize();

        // Prevent path traversal
        if (!filePath.startsWith(baseStoragePath)) {
            throw new IllegalArgumentException(
                    "Invalid storage key"
            );
        }

        try {

            Files.deleteIfExists(filePath);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not delete file",
                    e
            );
        }
    }

    private String getExtension(
            String filename,
            String contentType
    ) {

        // First try filename extension
        int lastDot = filename.lastIndexOf('.');

        if (lastDot >= 0 &&
                lastDot < filename.length() - 1) {

            String extension =
                    filename.substring(lastDot).toLowerCase();

            if (extension.matches(
                    "\\.(jpg|jpeg|png|webp)"
            )) {
                return extension;
            }
        }

        // Otherwise use content type
        if (contentType == null) {
            throw new IllegalArgumentException(
                    "Could not determine image format"
            );
        }

        return switch (contentType) {

            case "image/jpeg" -> ".jpg";

            case "image/png" -> ".png";

            case "image/webp" -> ".webp";

            default -> throw new IllegalArgumentException(
                    "Unsupported image format"
            );
        };
    }
}