package Rastoder.HoneyManagementSystem.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    // Where files live on disk and under which URL prefix WebConfig serves them.
    private enum Area {
        IMAGES("uploads/images", "/images/"),
        RECEIPTS("uploads/receipts", "/receipts/");

        private final Path directory;
        private final String publicPrefix;

        Area(String directory, String publicPrefix) {
            this.directory = Path.of(directory).toAbsolutePath().normalize();
            this.publicPrefix = publicPrefix;
        }
    }

    // Only these types are accepted. The file extension is chosen here, never
    // taken from the uploaded filename, so the server always serves the file
    // with a safe content type (e.g. no .html or .svg that could run JavaScript).
    private static final Map<String, String> IMAGE_TYPES = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/webp", "webp"
    );

    private static final Map<String, String> RECEIPT_TYPES = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/webp", "webp",
            "application/pdf", "pdf"
    );

    private static final byte[] PNG_MAGIC = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] RIFF = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] WEBP = "WEBP".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PDF_MAGIC = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    public FileStorageService() {
        try {
            for (Area area : Area.values()) {
                Files.createDirectories(area.directory);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not initialize storage directory.", e);
        }
    }

    /**
     * Stores a product image (PNG, JPEG, WebP) and returns its public URL.
     */
    public String saveImage(MultipartFile file) {
        return store(file, Area.IMAGES, IMAGE_TYPES, "Only PNG, JPEG or WebP images are allowed.");
    }

    /**
     * Stores an expense receipt (PDF or PNG, JPEG, WebP photo) and returns its public URL.
     */
    public String saveReceipt(MultipartFile file) {
        return store(file, Area.RECEIPTS, RECEIPT_TYPES, "Only PDF, PNG, JPEG or WebP files are allowed.");
    }

    /**
     * Deletes a file previously returned by {@link #saveImage} or {@link #saveReceipt}.
     * Failures are only logged, because a leftover file must never break the calling operation.
     */
    public void deleteFile(String publicUrl) {
        if (publicUrl == null) {
            return;
        }
        for (Area area : Area.values()) {
            if (publicUrl.startsWith(area.publicPrefix)) {
                Path target = area.directory.resolve(publicUrl.substring(area.publicPrefix.length())).normalize();
                if (!area.directory.equals(target.getParent())) {
                    return;
                }
                try {
                    Files.deleteIfExists(target);
                } catch (IOException e) {
                    log.warn("Could not delete file {}", target, e);
                }
                return;
            }
        }
    }

    /**
     * A new file is written to disk immediately, but the database row pointing to it
     * is only committed at the end of the transaction. If that commit fails, remove
     * the file again so no orphaned files pile up.
     */
    public void deleteOnRollback(String publicUrl) {
        runAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK, publicUrl);
    }

    /**
     * An old file that is replaced or no longer referenced is only removed once the
     * database change is committed, so a rollback never leaves a row pointing to a
     * file that is already gone.
     */
    public void deleteAfterCommit(String publicUrl) {
        if (publicUrl == null) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteFile(publicUrl);
            return;
        }
        runAfterCompletion(TransactionSynchronization.STATUS_COMMITTED, publicUrl);
    }

    private void runAfterCompletion(int expectedStatus, String publicUrl) {
        if (publicUrl == null || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == expectedStatus) {
                    deleteFile(publicUrl);
                }
            }
        });
    }

    private String store(MultipartFile file, Area area, Map<String, String> allowedTypes, String typeError) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file.");
        }

        String extension = allowedTypes.get(file.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException(typeError);
        }

        try {
            // The Content-Type header is chosen by the client, so also check the
            // first bytes of the file to make sure it really is that format.
            if (!hasMatchingSignature(file, extension)) {
                throw new IllegalArgumentException("File content does not match its type.");
            }

            String filename = UUID.randomUUID() + "." + extension;
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, area.directory.resolve(filename));
            }
            return area.publicPrefix + filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Fehler beim Speichern der Datei.", e);
        }
    }

    private boolean hasMatchingSignature(MultipartFile file, String extension) throws IOException {
        byte[] header;
        try (InputStream in = file.getInputStream()) {
            header = in.readNBytes(12);
        }
        return switch (extension) {
            case "png" -> startsWith(header, 0, PNG_MAGIC);
            case "jpg" -> startsWith(header, 0, JPEG_MAGIC);
            case "webp" -> startsWith(header, 0, RIFF) && startsWith(header, 8, WEBP);
            case "pdf" -> startsWith(header, 0, PDF_MAGIC);
            default -> false;
        };
    }

    private static boolean startsWith(byte[] data, int offset, byte[] prefix) {
        return data.length >= offset + prefix.length
                && Arrays.equals(data, offset, offset + prefix.length, prefix, 0, prefix.length);
    }
}
