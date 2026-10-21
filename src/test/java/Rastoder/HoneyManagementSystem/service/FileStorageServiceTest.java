package Rastoder.HoneyManagementSystem.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    private static final byte[] PNG_BYTES = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0};
    private static final byte[] JPEG_BYTES = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};

    private final FileStorageService fileStorageService = new FileStorageService();

    @Test
    @DisplayName("saveImage: With valid PNG, should store under a generated name and return path")
    void saveImage_WithValidPng_ShouldReturnPath() {
        // Given
        var file = new MockMultipartFile("file", "honey.png", "image/png", PNG_BYTES);

        // When
        String path = fileStorageService.saveImage(file);

        // Then
        assertThat(path).startsWith("/images/").endsWith(".png").doesNotContain("honey");
        assertThat(storedFile(path)).exists();
        fileStorageService.deleteImage(path);
    }

    @Test
    @DisplayName("saveImage: With valid JPEG, should use .jpg extension")
    void saveImage_WithValidJpeg_ShouldReturnJpgPath() {
        // Given
        var file = new MockMultipartFile("file", "honey.jpeg", "image/jpeg", JPEG_BYTES);

        // When
        String path = fileStorageService.saveImage(file);

        // Then
        assertThat(path).endsWith(".jpg");
        fileStorageService.deleteImage(path);
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
    @DisplayName("saveImage: When content type is not an allowed image, should throw IllegalArgumentException")
    void saveImage_WithDisallowedContentType_ShouldThrowException() {
        // Given
        var file = new MockMultipartFile("file", "evil.html", "text/html",
                "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8));

        // When / Then
        assertThatThrownBy(() -> fileStorageService.saveImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only PNG, JPEG or WebP images are allowed.");
    }

    @Test
    @DisplayName("saveImage: When content does not match claimed image type, should throw IllegalArgumentException")
    void saveImage_WithSpoofedContentType_ShouldThrowException() {
        // Given - claims to be PNG but is actually HTML
        var file = new MockMultipartFile("file", "honey.png", "image/png",
                "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8));

        // When / Then
        assertThatThrownBy(() -> fileStorageService.saveImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File content does not match its image type.");
    }

    @Test
    @DisplayName("saveImage: Path traversal in original filename is ignored because the name is generated")
    void saveImage_WithPathTraversalName_ShouldStoreInsideUploadDirectory() {
        // Given
        var file = new MockMultipartFile("file", "../../../etc/passwd", "image/png", PNG_BYTES);

        // When
        String path = fileStorageService.saveImage(file);

        // Then
        assertThat(path).startsWith("/images/").doesNotContain("..").doesNotContain("passwd");
        assertThat(storedFile(path)).exists();
        fileStorageService.deleteImage(path);
    }

    @Test
    @DisplayName("deleteImage: Should remove a previously stored image")
    void deleteImage_ShouldRemoveFile() {
        // Given
        String path = fileStorageService.saveImage(
                new MockMultipartFile("file", "honey.png", "image/png", PNG_BYTES));

        // When
        fileStorageService.deleteImage(path);

        // Then
        assertThat(storedFile(path)).doesNotExist();
    }

    @Test
    @DisplayName("deleteImage: Should ignore paths outside the upload directory")
    void deleteImage_WithTraversalPath_ShouldDoNothing() {
        // When / Then - must not throw and must not touch anything outside uploads/images
        fileStorageService.deleteImage("/images/../../pom.xml");
        fileStorageService.deleteImage(null);

        assertThat(Path.of("pom.xml")).exists();
    }

    private static Path storedFile(String publicPath) {
        return Path.of("uploads/images", publicPath.substring("/images/".length()));
    }
}
