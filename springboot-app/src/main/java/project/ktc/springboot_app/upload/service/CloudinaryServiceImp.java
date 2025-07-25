package project.ktc.springboot_app.upload.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.upload.dto.ImageUploadResponseDto;
import project.ktc.springboot_app.upload.dto.VideoUploadResponseDto;
import project.ktc.springboot_app.upload.exception.ImageUploadException;
import project.ktc.springboot_app.upload.exception.InvalidImageFormatException;
import project.ktc.springboot_app.upload.exception.VideoUploadException;
import project.ktc.springboot_app.upload.interfaces.CloudinaryService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling image uploads to Cloudinary
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImp implements CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload image file to Cloudinary
     * 
     * @param file MultipartFile to upload
     * @return ImageUploadResponseDto with upload details
     * @throws ImageUploadException        if upload fails
     * @throws InvalidImageFormatException if file format is not supported
     */
    @Override
    public ImageUploadResponseDto uploadImage(MultipartFile file) {
        log.info("Starting image upload for file: {}", file.getOriginalFilename());

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
     * /**
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
     * Upload video file to Cloudinary
     * 
     * @param file MultipartFile to upload
     * @return VideoUploadResponseDto with upload details
     * @throws VideoUploadException if upload fails
     */
    @Override
    public VideoUploadResponseDto uploadVideo(MultipartFile file) {
        log.info("Starting video upload for file: {}", file.getOriginalFilename());

        try {
            // Generate unique public ID for video
            String publicId = generatePublicId(file.getOriginalFilename());

            // Upload to Cloudinary with video-specific options
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "course-videos", // Organize video uploads in separate folder
                            "resource_type", "video", // Specify that this is a video
                            "quality", "auto", // Auto quality optimization
                            "format", "mp4" // Convert to MP4 for compatibility
                    ));

            log.info("Video upload successful. Public ID: {}, Secure URL: {}",
                    uploadResult.get("public_id"), uploadResult.get("secure_url"));

            return buildVideoResponseDto(uploadResult, file);

        } catch (IOException e) {
            log.error("Failed to upload video: {}", file.getOriginalFilename(), e);
            throw new VideoUploadException("Failed to upload video to cloud storage", e);
        }
    }

    /**
     * Delete video from Cloudinary
     * 
     * @param publicId Public ID of the video to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteVideo(String publicId) {
        log.info("Attempting to delete video with public ID: {}", publicId);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", "video"));

            String resultStatus = (String) result.get("result");
            boolean success = "ok".equals(resultStatus);

            log.info("Video deletion result for {}: {} ({})", publicId, success, resultStatus);
            return success;

        } catch (IOException e) {
            log.error("Failed to delete video with public ID: {}", publicId, e);
            return false;
        }
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

    /**
     * Build video response DTO from Cloudinary upload result
     */
    private VideoUploadResponseDto buildVideoResponseDto(Map<String, Object> uploadResult, MultipartFile file) {
        return VideoUploadResponseDto.builder()
                .secureUrl((String) uploadResult.get("secure_url"))
                .publicId((String) uploadResult.get("public_id"))
                .originalFilename(file.getOriginalFilename())
                .sizeInBytes(file.getSize())
                .duration(uploadResult.get("duration") != null
                        ? Double.parseDouble(uploadResult.get("duration").toString())
                        : null)
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .format((String) uploadResult.get("format"))
                .resourceType((String) uploadResult.get("resource_type"))
                .uploadedAt(LocalDateTime.now())
                .build();
    }
}
