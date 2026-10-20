
package Rastoder.HoneyManagementSystem.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageDirectory =
            Path.of("uploads/images").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not initialize storage directory.", e);
        }
    }

    public String saveImage(MultipartFile file) {
        if (file == null
                || file.isEmpty()
                || file.getOriginalFilename() == null
                || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException(
                    "Cannot store empty file.");
        }

        String originalFilename = file.getOriginalFilename();

        // Reject path traversal before cleaning the filename.
        if (originalFilename.contains("..")
                || originalFilename.contains("/")
                || originalFilename.contains("\\")) {
            throw new SecurityException(
                    "Cannot store file outside current directory.");
        }

        String cleanFilename = StringUtils.cleanPath(originalFilename);

        if (cleanFilename.isBlank()
                || cleanFilename.equals(".")
                || cleanFilename.equals("..")) {
            throw new IllegalArgumentException(
                    "Cannot store empty file.");
        }

        String filename = UUID.randomUUID()
                + "_"
                + Path.of(cleanFilename).getFileName();

        try {
            Path destination = storageDirectory.resolve(filename)
                    .normalize();

            if (!destination.startsWith(storageDirectory)) {
                throw new SecurityException(
                        "Cannot store file outside current directory.");
            }

            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING);

            return "/images/" + filename;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Fehler beim Speichern der Datei.", e);
        }
    }
}
