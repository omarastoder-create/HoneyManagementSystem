package Rastoder.HoneyManagementSystem.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path storageDirectory = Paths.get("uploads/images");

    public FileStorageService() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Upload-Verzeichnis konnte nicht erstellt werden.", e);
        }
    }

    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // UUID verhindert das Überschreiben gleichnamiger Dateien
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destination = storageDirectory.resolve(filename);
            Files.copy(file.getInputStream(), destination);

            // Rückgabe des relativen Pfads für das Frontend
            return "/images/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Speichern der Datei.", e);
        }
    }
}