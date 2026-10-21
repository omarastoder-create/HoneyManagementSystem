package Rastoder.HoneyManagementSystem.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    private static final String PUBLIC_PREFIX = "/images/";

    // Only real raster images are accepted. The file extension is chosen here,
    // never taken from the uploaded filename, so the server always serves the
    // file with an image content type (e.g. no .html or .svg that could run JavaScript).
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/webp", "webp"
    );

    private static final byte[] PNG_MAGIC = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] RIFF = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] WEBP = "WEBP".getBytes(StandardCharsets.US_ASCII);

    private final Path storageDirectory =
            Path.of("uploads/images").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not initialize storage directory.", e);
        }
    }

    /**
     * Stores the image under a server-generated name and returns its public URL.
     */
    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file.");
        }

        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("Only PNG, JPEG or WebP images are allowed.");
        }

        try {
            // The Content-Type header is chosen by the client, so also check the
            // first bytes of the file to make sure it really is that image format.
            if (!hasMatchingSignature(file, extension)) {
                throw new IllegalArgumentException("File content does not match its image type.");
            }

            String filename = UUID.randomUUID() + "." + extension;
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, storageDirectory.resolve(filename));
            }
            return PUBLIC_PREFIX + filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Fehler beim Speichern der Datei.", e);
        }
    }

    /**
     * Deletes an image previously returned by {@link #saveImage}. Failures are only
     * logged, because a leftover file must never break the calling operation.
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(PUBLIC_PREFIX)) {
            return;
        }
        Path target = storageDirectory.resolve(imageUrl.substring(PUBLIC_PREFIX.length())).normalize();
        if (!target.getParent().equals(storageDirectory)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Could not delete image {}", target, e);
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
            default -> false;
        };
    }

    private static boolean startsWith(byte[] data, int offset, byte[] prefix) {
        return data.length >= offset + prefix.length
                && Arrays.equals(data, offset, offset + prefix.length, prefix, 0, prefix.length);
    }
}
