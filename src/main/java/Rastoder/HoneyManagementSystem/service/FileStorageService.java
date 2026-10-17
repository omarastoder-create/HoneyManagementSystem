package Rastoder.HoneyManagementSystem.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageDirectory = Path.of("uploads/images");

    public FileStorageService() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory.", e);
        }
    }

    public String saveImage(MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("Cannot store empty file.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "_" + Path.of(originalFilename).getFileName().toString();

        try {
            Path destination = storageDirectory.resolve(filename).normalize().toAbsolutePath();

            // Security check to prevent path traversal outside storage directory
            if (!destination.startsWith(storageDirectory.toAbsolutePath().normalize())) {
                throw new SecurityException("Cannot store file outside current directory.");
            }

            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return "/images/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Speichern der Datei.", e);
        }
    }
}