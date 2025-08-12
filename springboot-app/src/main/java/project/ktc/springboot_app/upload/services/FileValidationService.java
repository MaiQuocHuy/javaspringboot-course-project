package project.ktc.springboot_app.upload.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.upload.exception.InvalidDocumentFormatException;
import project.ktc.springboot_app.upload.exception.InvalidImageFormatException;
import project.ktc.springboot_app.upload.exception.InvalidVideoFormatException;

import java.util.List;

/**
 * Service for validating uploaded files
 */
@Service
@Slf4j
public class FileValidationService {

    // Allowed image MIME types
    private static final List<String> ALLOWED_IMAGE_MIME_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/jpg",
            "image/webp");

    // Allowed video MIME types
    private static final List<String> ALLOWED_VIDEO_MIME_TYPES = List.of(
            "video/mp4",
            "video/mpeg",
            "video/quicktime",
            "video/x-msvideo", // AVI
            "video/x-ms-wmv", // WMV
            "video/webm",
            "video/x-matroska", // MKV
            "video/ogg");

    // Allowed document MIME types for instructor applications
    private static final List<String> ALLOWED_DOCUMENT_MIME_TYPES = List.of(
            "application/pdf", // PDF files
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
            "application/msword", // DOC
            "image/jpeg", // JPG/JPEG for scanned documents
            "image/png" // PNG for scanned documents
    );

    // Maximum file size for images (10MB)
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    // Maximum file size for videos (100MB)
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;

    // Maximum file size for documents (15MB)
    private static final long MAX_DOCUMENT_SIZE = 15 * 1024 * 1024;

    /**
     * Validate uploaded image file
     * 
     * @param file MultipartFile to validate
     * @throws InvalidImageFormatException if validation fails
     */
    public void validateImageFile(MultipartFile file) {
        log.debug("Starting validation for image file: {}", file.getOriginalFilename());

        validateFileNotEmpty(file);
        validateImageFileSize(file);
        validateImageMimeType(file);

        log.debug("Image file validation passed for: {} ({})",
                file.getOriginalFilename(), file.getContentType());
    }

    /**
     * Validate uploaded video file
     * 
     * @param file MultipartFile to validate
     * @throws InvalidVideoFormatException if validation fails
     */
    public void validateVideoFile(MultipartFile file) {
        log.debug("Starting validation for video file: {}", file.getOriginalFilename());

        validateFileNotEmpty(file);
        validateVideoFileSize(file);
        validateVideoMimeType(file);

        log.debug("Video file validation passed for: {} ({})",
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
     * Check if image file size is within limits
     */
    private void validateImageFileSize(MultipartFile file) {
        long fileSizeInMB = file.getSize() / (1024 * 1024);
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new InvalidImageFormatException(
                    String.format("Image file size (%d MB) exceeds maximum allowed size of %d MB",
                            fileSizeInMB, MAX_IMAGE_SIZE / (1024 * 1024)));
        }
    }

    /**
     * Check if video file size is within limits
     */
    private void validateVideoFileSize(MultipartFile file) {
        long fileSizeInMB = file.getSize() / (1024 * 1024);
        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new InvalidVideoFormatException(
                    String.format("Video file size (%d MB) exceeds maximum allowed size of %d MB",
                            fileSizeInMB, MAX_VIDEO_SIZE / (1024 * 1024)));
        }
    }

    /**
     * Check if image MIME type is allowed
     */
    private void validateImageMimeType(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new InvalidImageFormatException("File content type could not be determined");
        }

        if (!ALLOWED_IMAGE_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidImageFormatException(
                    String.format("Invalid image format '%s'. Allowed formats: %s",
                            contentType, String.join(", ", ALLOWED_IMAGE_MIME_TYPES)));
        }
    }

    /**
     * Check if video MIME type is allowed
     */
    private void validateVideoMimeType(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new InvalidVideoFormatException("File content type could not be determined");
        }

        if (!ALLOWED_VIDEO_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidVideoFormatException(
                    String.format("Invalid video format '%s'. Allowed formats: %s",
                            contentType, String.join(", ", ALLOWED_VIDEO_MIME_TYPES)));
        }
    }

    /**
     * Get allowed image MIME types (for documentation purposes)
     */
    public List<String> getAllowedImageMimeTypes() {
        return List.copyOf(ALLOWED_IMAGE_MIME_TYPES);
    }

    /**
     * Validate uploaded document file for instructor applications
     * 
     * @param file MultipartFile to validate
     * @throws InvalidDocumentFormatException if validation fails
     */
    public void validateDocumentFile(MultipartFile file) {
        log.debug("Starting validation for document file: {}", file.getOriginalFilename());

        // Check if file is empty
        if (file.isEmpty()) {
            throw new InvalidDocumentFormatException("Document file is required and cannot be empty");
        }

        // Check file size
        if (file.getSize() > MAX_DOCUMENT_SIZE) {
            throw new InvalidDocumentFormatException(
                    String.format("Document file size exceeds maximum allowed size of %d MB. Current size: %.2f MB",
                            MAX_DOCUMENT_SIZE / (1024 * 1024),
                            (double) file.getSize() / (1024 * 1024)));
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_DOCUMENT_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidDocumentFormatException(
                    String.format("Unsupported document format: %s. Allowed formats: %s",
                            contentType,
                            String.join(", ", ALLOWED_DOCUMENT_MIME_TYPES)));
        }

        log.debug("Document file validation passed for: {}", file.getOriginalFilename());
    }

    /**
     * Get allowed document MIME types (for documentation purposes)
     */
    public List<String> getAllowedDocumentMimeTypes() {
        return List.copyOf(ALLOWED_DOCUMENT_MIME_TYPES);
    }

    /**
     * Get maximum document file size in MB (for documentation purposes)
     */
    public long getMaxDocumentFileSizeMB() {
        return MAX_DOCUMENT_SIZE / (1024 * 1024);
    }

    /**
     * Get allowed video MIME types (for documentation purposes)
     */
    public List<String> getAllowedVideoMimeTypes() {
        return List.copyOf(ALLOWED_VIDEO_MIME_TYPES);
    }

    /**
     * Get maximum image file size in MB (for documentation purposes)
     */
    public long getMaxImageFileSizeMB() {
        return MAX_IMAGE_SIZE / (1024 * 1024);
    }

    /**
     * Get maximum video file size in MB (for documentation purposes)
     */
    public long getMaxVideoFileSizeMB() {
        return MAX_VIDEO_SIZE / (1024 * 1024);
    }
}
