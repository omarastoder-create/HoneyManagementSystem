package Rastoder.HoneyManagementSystem.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    private static final byte[] PNG_BYTES = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0};
    private static final byte[] JPEG_BYTES = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    private static final byte[] PDF_BYTES = "%PDF-1.7\n%âãÏÓ\n".getBytes(StandardCharsets.ISO_8859_1);

    private final FileStorageService fileStorageService = new FileStorageService("uploads");

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
        fileStorageService.deleteFile(path);
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
        fileStorageService.deleteFile(path);
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
                .hasMessage("File content does not match its type.");
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
        fileStorageService.deleteFile(path);
    }

    @Test
    @DisplayName("saveReceipt: With valid PDF, should store under /receipts/ with .pdf extension")
    void saveReceipt_WithValidPdf_ShouldReturnReceiptPath() {
        // Given
        var file = new MockMultipartFile("file", "rechnung.pdf", "application/pdf", PDF_BYTES);

        // When
        String path = fileStorageService.saveReceipt(file);

        // Then
        assertThat(path).startsWith("/receipts/").endsWith(".pdf");
        assertThat(storedFile(path)).exists();
        fileStorageService.deleteFile(path);
        assertThat(storedFile(path)).doesNotExist();
    }

    @Test
    @DisplayName("saveReceipt: Photo of a receipt (JPEG) should also be accepted")
    void saveReceipt_WithJpeg_ShouldReturnReceiptPath() {
        // When
        String path = fileStorageService.saveReceipt(
                new MockMultipartFile("file", "beleg.jpg", "image/jpeg", JPEG_BYTES));

        // Then
        assertThat(path).startsWith("/receipts/").endsWith(".jpg");
        fileStorageService.deleteFile(path);
    }

    @Test
    @DisplayName("saveReceipt: Fake PDF should be rejected")
    void saveReceipt_WithSpoofedPdf_ShouldThrowException() {
        // Given
        var file = new MockMultipartFile("file", "rechnung.pdf", "application/pdf",
                "<html></html>".getBytes(StandardCharsets.UTF_8));

        // When / Then
        assertThatThrownBy(() -> fileStorageService.saveReceipt(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File content does not match its type.");
    }

    @Test
    @DisplayName("saveImage: PDF is allowed for receipts but not as a product image")
    void saveImage_WithPdf_ShouldThrowException() {
        // Given
        var file = new MockMultipartFile("file", "honey.pdf", "application/pdf", PDF_BYTES);

        // When / Then
        assertThatThrownBy(() -> fileStorageService.saveImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only PNG, JPEG or WebP images are allowed.");
    }

    @Test
    @DisplayName("deleteImage: Should remove a previously stored image")
    void deleteImage_ShouldRemoveFile() {
        // Given
        String path = fileStorageService.saveImage(
                new MockMultipartFile("file", "honey.png", "image/png", PNG_BYTES));

        // When
        fileStorageService.deleteFile(path);

        // Then
        assertThat(storedFile(path)).doesNotExist();
    }

    @Test
    @DisplayName("deleteImage: Should ignore paths outside the upload directory")
    void deleteImage_WithTraversalPath_ShouldDoNothing() {
        // When / Then - must not throw and must not touch anything outside uploads/images
        fileStorageService.deleteFile("/images/../../pom.xml");
        fileStorageService.deleteFile(null);

        assertThat(Path.of("pom.xml")).exists();
    }

    @Test
    @DisplayName("saveImage: WebP images are accepted")
    void saveImage_WithWebp_ShouldReturnWebpPath() {
        // Given - RIFF....WEBP header
        byte[] webp = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 0};

        // When
        String path = fileStorageService.saveImage(new MockMultipartFile("file", "honey.webp", "image/webp", webp));

        // Then
        assertThat(path).endsWith(".webp");
        fileStorageService.deleteFile(path);
    }

    @Test
    @DisplayName("saveReceipt: Unsupported type should name the allowed receipt formats")
    void saveReceipt_WithUnsupportedType_ShouldThrow() {
        var file = new MockMultipartFile("file", "beleg.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> fileStorageService.saveReceipt(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only PDF, PNG, JPEG or WebP files are allowed.");
    }

    @Test
    @DisplayName("deleteFile: Unknown URL prefixes are ignored")
    void deleteFile_WithUnknownPrefix_ShouldDoNothing() {
        fileStorageService.deleteFile("/other/pom.xml");

        assertThat(Path.of("pom.xml")).exists();
    }

    @Test
    @DisplayName("deleteOnRollback: File is removed when the transaction rolls back, kept when it commits")
    void deleteOnRollback_ShouldOnlyDeleteOnRollback() {
        String rolledBack = fileStorageService.saveReceipt(pdf());
        String committed = fileStorageService.saveReceipt(pdf());

        inTransaction(() -> fileStorageService.deleteOnRollback(rolledBack), TransactionSynchronization.STATUS_ROLLED_BACK);
        inTransaction(() -> fileStorageService.deleteOnRollback(committed), TransactionSynchronization.STATUS_COMMITTED);

        assertThat(storedFile(rolledBack)).doesNotExist();
        assertThat(storedFile(committed)).exists();
        fileStorageService.deleteFile(committed);
    }

    @Test
    @DisplayName("deleteAfterCommit: File is removed only after a successful commit")
    void deleteAfterCommit_ShouldOnlyDeleteOnCommit() {
        String committed = fileStorageService.saveReceipt(pdf());
        String rolledBack = fileStorageService.saveReceipt(pdf());

        inTransaction(() -> fileStorageService.deleteAfterCommit(committed), TransactionSynchronization.STATUS_COMMITTED);
        inTransaction(() -> fileStorageService.deleteAfterCommit(rolledBack), TransactionSynchronization.STATUS_ROLLED_BACK);

        assertThat(storedFile(committed)).doesNotExist();
        assertThat(storedFile(rolledBack)).exists();
        fileStorageService.deleteFile(rolledBack);
    }

    @Test
    @DisplayName("deleteAfterCommit: Without a transaction the file is deleted immediately; null is ignored")
    void deleteAfterCommit_WithoutTransaction_ShouldDeleteImmediately() {
        String path = fileStorageService.saveReceipt(pdf());

        fileStorageService.deleteAfterCommit(path);
        fileStorageService.deleteAfterCommit(null);
        fileStorageService.deleteOnRollback(null);

        assertThat(storedFile(path)).doesNotExist();
    }

    private static MockMultipartFile pdf() {
        return new MockMultipartFile("file", "r.pdf", "application/pdf", PDF_BYTES);
    }

    // Simulates what Spring's transaction manager does: register callbacks, then complete with the given status.
    private static void inTransaction(Runnable work, int completionStatus) {
        TransactionSynchronizationManager.initSynchronization();
        try {
            work.run();
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(sync -> sync.afterCompletion(completionStatus));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // "/images/x.png" -> uploads/images/x.png, "/receipts/x.pdf" -> uploads/receipts/x.pdf
    private static Path storedFile(String publicPath) {
        return Path.of("uploads", publicPath.substring(1));
    }
}
