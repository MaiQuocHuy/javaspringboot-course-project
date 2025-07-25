package project.ktc.springboot_app.upload.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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

    // Maximum file size for images (10MB)
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    // Maximum file size for videos (100MB)
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;

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
