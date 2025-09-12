package project.ktc.springboot_app.upload.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.upload.dto.AudioUploadResponseDto;
import project.ktc.springboot_app.upload.dto.DocumentUploadResponseDto;
import project.ktc.springboot_app.upload.dto.ImageUploadResponseDto;
import project.ktc.springboot_app.upload.dto.VideoMetadataResponseDto;
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
        String extension = "";

        // Remove file extension as Cloudinary handles it
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = filename.substring(lastDotIndex);
            filename = filename.substring(0, lastDotIndex);
        }

        return String.format("%s_%s_%s%s", filename, timestamp, uuid, extension);
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
     * Upload audio file to Cloudinary
     * 
     * @param file MultipartFile to upload
     * @return AudioUploadResponseDto with upload details
     * @throws IOException if upload fails
     */
    @Override
    public AudioUploadResponseDto uploadAudio(MultipartFile file) {
        log.info("Starting audio upload for file: {}", file.getOriginalFilename());

        try {
            // Generate unique public ID for audio
            String publicId = generatePublicId(file.getOriginalFilename());

            // Upload to Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "course-audio",
                            "resource_type", "video", // Audio is uploaded as video resource type in Cloudinary
                            "quality", "auto",
                            "format", "mp3" // Convert to MP3 for consistency
                    ));

            log.info("Audio upload successful. Public ID: {}, URL: {}",
                    uploadResult.get("public_id"), uploadResult.get("secure_url"));

            return buildAudioResponseDto(uploadResult, file);

        } catch (IOException e) {
            log.error("Failed to upload audio: {}", e.getMessage(), e);
            throw new RuntimeException("Audio upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Delete audio from Cloudinary
     * 
     * @param publicId Public ID of the audio to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteAudio(String publicId) {
        log.info("Deleting audio with public ID: {}", publicId);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", "video")); // Audio is stored as video resource type

            String resultStatus = (String) result.get("result");
            boolean success = "ok".equals(resultStatus);

            log.info("Audio deletion result for {}: {} ({})", publicId, success, resultStatus);
            return success;

        } catch (IOException e) {
            log.error("Failed to delete audio with public ID: {}", publicId, e);
            return false;
        }
    }

    /**
     * Build audio response DTO from Cloudinary upload result
     */
    private AudioUploadResponseDto buildAudioResponseDto(Map<String, Object> uploadResult, MultipartFile file) {
        return AudioUploadResponseDto.builder()
                .url((String) uploadResult.get("secure_url"))
                .publicId((String) uploadResult.get("public_id"))
                .fileName(file.getOriginalFilename())
                .size(file.getSize())
                .format((String) uploadResult.get("format"))
                .duration(extractAudioDuration(uploadResult))
                .bitrate(extractAudioBitrate(uploadResult))
                .sampleRate(extractAudioSampleRate(uploadResult))
                .channels(extractAudioChannels(uploadResult))
                .uploadedAt(LocalDateTime.now())
                .mimeType(file.getContentType())
                .build();
    }

    private Double extractAudioDuration(Map<String, Object> uploadResult) {
        Object duration = uploadResult.get("duration");
        if (duration instanceof Number) {
            return ((Number) duration).doubleValue();
        }
        return null;
    }

    private Integer extractAudioBitrate(Map<String, Object> uploadResult) {
        Object bitrate = uploadResult.get("bit_rate");
        if (bitrate instanceof Number) {
            return ((Number) bitrate).intValue();
        }
        return null;
    }

    private Integer extractAudioSampleRate(Map<String, Object> uploadResult) {
        Object sampleRate = uploadResult.get("audio_frequency");
        if (sampleRate instanceof Number) {
            return ((Number) sampleRate).intValue();
        }
        return null;
    }

    private Integer extractAudioChannels(Map<String, Object> uploadResult) {
        Object channels = uploadResult.get("audio_channels");
        if (channels instanceof Number) {
            return ((Number) channels).intValue();
        }
        return null;
    }

    /**
     * Upload document file to Cloudinary
     * 
     * @param file MultipartFile to upload
     * @return DocumentUploadResponseDto with upload details
     * @throws IOException if upload fails
     */
    @Override
    public DocumentUploadResponseDto uploadDocument(MultipartFile file) {
        log.info("Starting document upload for file: {}", file.getOriginalFilename());

        try {
            // Generate unique public ID for documents
            String publicId = "instructor-documents/" + generatePublicId(file.getOriginalFilename());

            // Upload to Cloudinary as raw file
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "raw" // Use raw for documents
                    ));

            log.info("Document upload successful. Public ID: {}, URL: {}",
                    uploadResult.get("public_id"), uploadResult.get("secure_url"));

            return buildDocumentResponseDto(uploadResult, file);

        } catch (IOException e) {
            log.error("Failed to upload document: {}", e.getMessage(), e);
            throw new RuntimeException("Document upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Delete document from Cloudinary
     * 
     * @param publicId Public ID of the document to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteDocument(String publicId) {
        log.info("Attempting to delete document with public ID: {}", publicId);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", "raw"));

            String resultStatus = (String) result.get("result");
            boolean success = "ok".equals(resultStatus);

            log.info("Document deletion result for {}: {} ({})", publicId, success, resultStatus);
            return success;

        } catch (Exception e) {
            log.error("Failed to delete document {}: {}", publicId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Build document response DTO from Cloudinary upload result
     */
    private DocumentUploadResponseDto buildDocumentResponseDto(Map<String, Object> uploadResult, MultipartFile file) {
        return DocumentUploadResponseDto.builder()
                .url((String) uploadResult.get("secure_url"))
                .publicId((String) uploadResult.get("public_id"))
                .originalFilename(file.getOriginalFilename())
                .size(file.getSize())
                .resourceType((String) uploadResult.get("resource_type"))
                .format((String) uploadResult.get("format"))
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

    /**
     * Retrieve video metadata from Cloudinary video URL or public ID
     * 
     * @param videoUrlOrPublicId Cloudinary video URL or public ID
     * @return VideoMetadataResponseDto containing video metadata
     * @throws IOException if metadata retrieval fails
     */
    @Override
    public VideoMetadataResponseDto getVideoMetadata(String videoUrlOrPublicId) throws IOException {
        log.info("Retrieving video metadata for: {}", videoUrlOrPublicId);

        try {
            // Extract public ID from URL if necessary
            String publicId = extractPublicIdFromUrl(videoUrlOrPublicId);

            // Get resource details from Cloudinary Admin API
            @SuppressWarnings("unchecked")
            Map<String, Object> resourceDetails = cloudinary.api().resource(publicId,
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "type", "upload"));

            log.info("Successfully retrieved metadata for video: {}", publicId);
            log.info("Resource details: {}", resourceDetails.toString());
            // Extract metadata from Cloudinary response
            String title = extractTitle(resourceDetails, publicId);
            String format = (String) resourceDetails.get("format");

            // Generate thumbnail URL
            String thumbnailUrl = generateThumbnailUrl(publicId);

            Integer width = (Integer) resourceDetails.get("width");
            Integer height = (Integer) resourceDetails.get("height");
            Long sizeBytes = resourceDetails.get("bytes") != null
                    ? Long.parseLong(resourceDetails.get("bytes").toString())
                    : null;

            String videoUrl = (String) resourceDetails.get("secure_url");

            return VideoMetadataResponseDto.builder()
                    .title(title)
                    .format(format)
                    .thumbnail(thumbnailUrl)
                    .width(width)
                    .height(height)
                    .sizeBytes(sizeBytes)
                    .videoUrl(videoUrl)
                    .publicId(publicId)
                    .retrievedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to retrieve video metadata for: {}", videoUrlOrPublicId, e);
            throw new IOException("Failed to retrieve video metadata: " + e.getMessage(), e);
        }
    }

    /**
     * Extract public ID from Cloudinary URL or return as-is if already a public ID
     * 
     * @param videoUrlOrPublicId Cloudinary URL or public ID
     * @return extracted public ID
     */
    private String extractPublicIdFromUrl(String videoUrlOrPublicId) {
        if (videoUrlOrPublicId == null || videoUrlOrPublicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Video URL or public ID cannot be null or empty");
        }

        // If it's already a public ID (no http/https), return as-is
        if (!videoUrlOrPublicId.startsWith("http")) {
            return videoUrlOrPublicId.trim();
        }

        // Extract public ID from Cloudinary URL
        // Example URL:
        // https://res.cloudinary.com/cloud_name/video/upload/v1234567890/folder/video_name.mp4
        try {
            String[] parts = videoUrlOrPublicId.split("/");
            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i]) && i + 1 < parts.length) {
                    // Skip version number if present (starts with 'v')
                    int startIndex = i + 1;
                    if (parts[startIndex].startsWith("v") && parts[startIndex].matches("v\\d+")) {
                        startIndex++;
                    }

                    // Join remaining parts and remove file extension
                    StringBuilder publicIdBuilder = new StringBuilder();
                    for (int j = startIndex; j < parts.length; j++) {
                        if (j > startIndex) {
                            publicIdBuilder.append("/");
                        }
                        publicIdBuilder.append(parts[j]);
                    }

                    String publicId = publicIdBuilder.toString();
                    // Remove file extension
                    int lastDotIndex = publicId.lastIndexOf('.');
                    if (lastDotIndex > 0) {
                        publicId = publicId.substring(0, lastDotIndex);
                    }

                    return publicId;
                }
            }

            throw new IllegalArgumentException("Invalid Cloudinary URL format: " + videoUrlOrPublicId);

        } catch (Exception e) {
            log.error("Failed to extract public ID from URL: {}", videoUrlOrPublicId, e);
            throw new IllegalArgumentException("Invalid Cloudinary URL format: " + videoUrlOrPublicId, e);
        }
    }

    /**
     * Extract title from resource details or fallback to public ID
     * 
     * @param resourceDetails Cloudinary resource details
     * @param publicId        Public ID as fallback
     * @return video title
     */
    private String extractTitle(Map<String, Object> resourceDetails, String publicId) {
        // Try to get original filename first
        String originalFilename = (String) resourceDetails.get("original_filename");
        if (originalFilename != null && !originalFilename.trim().isEmpty()) {
            return originalFilename;
        }

        // Fallback to public ID, extract filename part
        String[] pathParts = publicId.split("/");
        String filename = pathParts[pathParts.length - 1];

        return filename;
    }

    /**
     * Generate thumbnail URL for video using Cloudinary transformation
     * 
     * @param publicId video public ID
     * @return thumbnail URL
     */
    private String generateThumbnailUrl(String publicId) {
        // Generate thumbnail URL with Cloudinary transformations
        // This creates a thumbnail from the middle of the video (50% position)
        @SuppressWarnings("rawtypes")
        Transformation transformation = new Transformation()
                .startOffset("50%")
                .quality("auto")
                .width(400)
                .height(300)
                .crop("fill");

        return cloudinary.url()
                .resourceType("video")
                .format("jpg")
                .transformation(transformation)
                .generate(publicId);
    }

    /**
     * Generate download URL for certificate PDF with attachment flag
     * This forces browsers to download the file instead of displaying it inline
     * 
     * @param publicId Cloudinary public ID
     * @param filename Original filename
     * @return Download URL with attachment flag
     */
    private String generateDownloadUrl(String publicId, String filename) {
        try {
            // Generate URL with fl_attachment flag to force download
            // For Cloudinary raw files, we need to modify the URL manually to add
            // attachment flag
            String baseUrl = cloudinary.url()
                    .resourceType("raw")
                    .generate(publicId);

            // Insert fl_attachment flag into the URL
            // URL format: https://res.cloudinary.com/cloud/raw/upload/v123/file.pdf
            // Target format:
            // https://res.cloudinary.com/cloud/raw/upload/fl_attachment/v123/file.pdf
            String downloadUrl = baseUrl.replace("/upload/", "/upload/fl_attachment/");

            log.info("Generated download URL for {}: {}", publicId, downloadUrl);
            return downloadUrl;

        } catch (Exception e) {
            log.error("Failed to generate download URL for {}: {}", publicId, e.getMessage(), e);
            // Fallback to regular URL if download URL generation fails
            return cloudinary.url()
                    .resourceType("raw")
                    .generate(publicId);
        }
    }

    /**
     * Upload PDF certificate to Cloudinary
     * 
     * @param pdfData  PDF file as byte array
     * @param filename Name of the PDF file
     * @return Document upload response with URL
     * @throws IOException if upload fails
     */
    @Override
    public DocumentUploadResponseDto uploadCertificatePdf(byte[] pdfData, String filename) throws IOException {
        log.info("Starting certificate PDF upload: {}", filename);

        try {
            // Generate unique public ID for certificates
            String publicId = "certificates/" + generatePublicId(filename);

            // Upload PDF to Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    pdfData,
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "raw", // Use raw for PDF files
                            "folder", "certificates",
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", false));

            log.info("Certificate PDF upload successful. Public ID: {}, URL: {}",
                    uploadResult.get("public_id"), uploadResult.get("secure_url"));

            // Generate download URL with attachment flag to force download
            String certPublicId = (String) uploadResult.get("public_id");
            String regularUrl = (String) uploadResult.get("secure_url");
            String downloadUrl = generateDownloadUrl(certPublicId, filename);

            log.info("Regular URL: {}", regularUrl);
            log.info("Download URL: {}", downloadUrl);

            return DocumentUploadResponseDto.builder()
                    .url(downloadUrl) // URL that forces download
                    .publicId((String) uploadResult.get("public_id"))
                    .originalFilename(filename)
                    .size((long) pdfData.length)
                    .resourceType((String) uploadResult.get("resource_type"))
                    .format((String) uploadResult.get("format"))
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload certificate PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Certificate PDF upload failed: " + e.getMessage(), e);
        }
    }
}
