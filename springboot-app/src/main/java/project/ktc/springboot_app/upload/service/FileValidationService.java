package project.ktc.springboot_app.upload.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.upload.exception.InvalidImageFormatException;

import java.util.List;

/**
 * Service for validating uploaded files
 */
@Service
@Slf4j
public class FileValidationService {

    // Allowed image MIME types
    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/webp");

    // Maximum file size (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Validate uploaded image file
     * 
     * @param file MultipartFile to validate
     * @throws InvalidImageFormatException if validation fails
     */
    public void validateImageFile(MultipartFile file) {
        log.debug("Starting validation for file: {}", file.getOriginalFilename());

        validateFileNotEmpty(file);
        validateFileSize(file);
        validateMimeType(file);

        log.debug("File validation passed for: {} ({})",
                file.getOriginalFilename(), file.getContentType());
    }

    /**
     * Check if file is not empty
     */
    private void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageFormatException("File is empty or not provided");
        }
    }

    /**
     * Check if file size is within limits
     */
    private void validateFileSize(MultipartFile file) {
        long fileSizeInMB = file.getSize() / (1024 * 1024);
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidImageFormatException(
                    String.format("File size (%d MB) exceeds maximum allowed size of %d MB",
                            fileSizeInMB, MAX_FILE_SIZE / (1024 * 1024)));
        }
    }

    /**
     * Check if MIME type is allowed
     */
    private void validateMimeType(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new InvalidImageFormatException("File content type could not be determined");
        }

        if (!ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidImageFormatException(
                    String.format("Invalid file format '%s'. Allowed formats: %s",
                            contentType, String.join(", ", ALLOWED_MIME_TYPES)));
        }
    }

    /**
     * Get allowed MIME types (for documentation purposes)
     */
    public List<String> getAllowedMimeTypes() {
        return List.copyOf(ALLOWED_MIME_TYPES);
    }

    /**
     * Get maximum file size in MB (for documentation purposes)
     */
    public long getMaxFileSizeMB() {
        return MAX_FILE_SIZE / (1024 * 1024);
    }
}
