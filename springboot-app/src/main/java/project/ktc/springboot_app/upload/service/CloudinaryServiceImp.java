package project.ktc.springboot_app.upload.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.upload.dto.ImageUploadResponseDto;
import project.ktc.springboot_app.upload.exception.ImageUploadException;
import project.ktc.springboot_app.upload.exception.InvalidImageFormatException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling image uploads to Cloudinary
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImp {

    private final Cloudinary cloudinary;

    // Allowed image formats
    private static final List<String> ALLOWED_FORMATS = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp");

    // Maximum file size (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Upload image file to Cloudinary
     * 
     * @param file MultipartFile to upload
     * @return ImageUploadResponseDto with upload details
     * @throws ImageUploadException        if upload fails
     * @throws InvalidImageFormatException if file format is not supported
     */
    public ImageUploadResponseDto uploadImage(MultipartFile file) {
        log.info("Starting image upload for file: {}", file.getOriginalFilename());

        // Validate file
        validateFile(file);

        try {
            // Generate unique public ID
            String publicId = generatePublicId(file.getOriginalFilename());

            // Upload to Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "course-management", // Organize uploads in folder
                            "resource_type", "image",
                            "quality", "auto:good", // Optimize image quality
                            "fetch_format", "auto" // Auto-optimize format
                    ));

            // Extract response data
            ImageUploadResponseDto response = buildResponseDto(uploadResult, file);

            log.info("Image uploaded successfully. URL: {}, Public ID: {}",
                    response.getUrl(), response.getPublicId());

            return response;

        } catch (IOException e) {
            log.error("Failed to upload image: {}", file.getOriginalFilename(), e);
            throw new ImageUploadException("Failed to upload image: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during image upload: {}", file.getOriginalFilename(), e);
            throw new ImageUploadException("Unexpected error occurred during upload", e);
        }
    }

    /**
     * Delete image from Cloudinary
     * 
     * @param publicId The public ID of the image to delete
     * @return true if deletion was successful
     */
    public boolean deleteImage(String publicId) {
        log.info("Deleting image with public ID: {}", publicId);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");

            boolean success = "ok".equals(resultStatus);
            if (success) {
                log.info("Image deleted successfully: {}", publicId);
            } else {
                log.warn("Image deletion failed or image not found: {}", publicId);
            }

            return success;

        } catch (Exception e) {
            log.error("Failed to delete image: {}", publicId, e);
            return false;
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new InvalidImageFormatException("File is empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidImageFormatException(
                    String.format("File size exceeds maximum allowed size of %d MB", MAX_FILE_SIZE / (1024 * 1024)));
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_FORMATS.contains(contentType.toLowerCase())) {
            throw new InvalidImageFormatException(
                    "Invalid file format. Allowed formats: " + String.join(", ", ALLOWED_FORMATS));
        }

        log.debug("File validation passed for: {} ({})", file.getOriginalFilename(), contentType);
    }

    /**
     * Generate unique public ID for the uploaded file
     */
    private String generatePublicId(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String filename = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") : "image";

        // Remove file extension as Cloudinary handles it
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            filename = filename.substring(0, lastDotIndex);
        }

        return String.format("%s_%s_%s", filename, timestamp, uuid);
    }

    /**
     * Build response DTO from Cloudinary upload result
     */
    private ImageUploadResponseDto buildResponseDto(Map<String, Object> uploadResult, MultipartFile file) {
        return ImageUploadResponseDto.builder()
                .url((String) uploadResult.get("secure_url"))
                .publicId((String) uploadResult.get("public_id"))
                .originalFilename(file.getOriginalFilename())
                .size(file.getSize())
                .format((String) uploadResult.get("format"))
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .build();
    }
}
