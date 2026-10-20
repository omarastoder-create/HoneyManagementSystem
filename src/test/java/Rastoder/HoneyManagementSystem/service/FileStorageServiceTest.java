package Rastoder.HoneyManagementSystem.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    private final FileStorageService fileStorageService = new FileStorageService();

    @Test
    @DisplayName("saveImage: With valid file, should store and return path")
    void saveImage_WithValidFile_ShouldReturnPath() {
        // Given
        var file = new MockMultipartFile("file", "honey.png", "image/png", new byte[]{1, 2, 3});

        // When
        String path = fileStorageService.saveImage(file);

        // Then
        assertThat(path).startsWith("/images/").endsWith("_honey.png");
    }

    @Test
    @DisplayName("saveImage: When file is null, should throw IllegalArgumentException")
    void saveImage_WhenFileIsNull_ShouldThrowException() {
        // When / Then
        assertThatThrownBy(() -> fileStorageService.saveImage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot store empty file.");
    }

    @Test
    @DisplayName("saveImage: When file is empty, should throw IllegalArgumentException")
    void saveImage_WhenFileIsEmpty_ShouldThrowException() {
        // Given
        var file = new MockMultipartFile("file", "", "image/png", new byte[0]);

        // When / Then
        assertThatThrownBy(() -> fileStorageService.saveImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot store empty file.");
    }

    @Test
    @DisplayName("saveImage: When filename is null, should throw IllegalArgumentException")
    void saveImage_WhenFilenameIsNull_ShouldThrowException() {
        // Given - Um einen echten null-Filenamen in Spring zu erzwingen, nutzen wir den Konstruktor mit Content-Type und byte[] ohne Namen
        var file = new MockMultipartFile("file", null, "image/png", new byte[]{1, 2});

        // When / Then
        assertThatThrownBy(() -> fileStorageService.saveImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot store empty file.");
    }

    @Test
    @DisplayName("saveImage: When path traversal is attempted, should throw SecurityException")
    void saveImage_WithPathTraversal_ShouldThrowSecurityException() {
        // Given - Verwende einen absoluten oder direkten Traversal-Namen ohne dass StringUtils.cleanPath ihn komplett harmlos weglässt
        // Alternativ erzwingen wir den Test direkt mit einem Pfad, der außerhalb aufgelöst wird:
        var file = new MockMultipartFile("file", "../../../etc/passwd", "image/png", new byte[]{1, 2});

        // When / Then
        assertThatThrownBy(() -> fileStorageService.saveImage(file))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Cannot store file outside current directory.");
    }
}