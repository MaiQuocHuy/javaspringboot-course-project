package project.ktc.springboot_app.upload.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.upload.dto.VideoMetadataResponseDto;
import project.ktc.springboot_app.upload.services.CloudinaryServiceImp;

import java.io.IOException;

/**
 * REST Controller for video metadata operations
 */
@RestController
@RequestMapping("/api/videos")
@Tag(name = "Video Metadata API", description = "Endpoints for retrieving video metadata from Cloudinary")
@RequiredArgsConstructor
@Validated
@Slf4j
public class VideoMetadataController {

    private final CloudinaryServiceImp cloudinaryService;

    /**
     * Retrieve video metadata by Cloudinary URL or public ID
     * 
     * @param videoUrlOrPublicId Cloudinary video URL or public ID
     * @return VideoMetadataResponseDto containing video metadata
     */
    @GetMapping("/metadata")
    @Operation(summary = "Get video metadata", description = "Retrieves comprehensive metadata for a video from Cloudinary including title, format, duration, thumbnail, dimensions, and file size")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video metadata retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid video URL or public ID format"),
            @ApiResponse(responseCode = "404", description = "Video not found on Cloudinary"),
            @ApiResponse(responseCode = "500", description = "Internal server error while retrieving metadata")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<VideoMetadataResponseDto>> getVideoMetadata(
            @Parameter(description = "Cloudinary video URL or public ID", example = "https://res.cloudinary.com/demo/video/upload/v1234567890/sample_video.mp4", required = true) @RequestParam @NotBlank(message = "Video URL or public ID cannot be blank") String videoUrlOrPublicId) {

        log.info("Received request to get video metadata for: {}", videoUrlOrPublicId);

        try {
            VideoMetadataResponseDto metadata = cloudinaryService.getVideoMetadata(videoUrlOrPublicId);

            log.info("Successfully retrieved metadata for video: {}", metadata.getPublicId());

            return ApiResponseUtil.success(
                    metadata,
                    "Video metadata retrieved successfully");

        } catch (IllegalArgumentException e) {
            log.warn("Invalid video URL or public ID format: {}", videoUrlOrPublicId, e);
            return ApiResponseUtil.error(
                    HttpStatus.BAD_REQUEST,
                    "Invalid video URL or public ID format: " + e.getMessage());

        } catch (IOException e) {
            log.error("Failed to retrieve video metadata for: {}", videoUrlOrPublicId, e);

            // Check if it's a "not found" error
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) {
                return ApiResponseUtil.error(
                        HttpStatus.NOT_FOUND,
                        "Video not found on Cloudinary: " + videoUrlOrPublicId);
            }

            return ApiResponseUtil.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve video metadata: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error while retrieving video metadata for: {}", videoUrlOrPublicId, e);
            return ApiResponseUtil.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error occurred while retrieving video metadata");
        }
    }

    /**
     * Retrieve video metadata by public ID (alternative endpoint)
     * 
     * @param publicId Cloudinary video public ID
     * @return VideoMetadataResponseDto containing video metadata
     */
    @GetMapping("/metadata/{publicId}")
    @Operation(summary = "Get video metadata by public ID", description = "Retrieves comprehensive metadata for a video using its Cloudinary public ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video metadata retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Video not found on Cloudinary"),
            @ApiResponse(responseCode = "500", description = "Internal server error while retrieving metadata")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<VideoMetadataResponseDto>> getVideoMetadataByPublicId(
            @Parameter(description = "Cloudinary video public ID", example = "videos/sample_video", required = true) @PathVariable @NotBlank(message = "Public ID cannot be blank") String publicId) {

        log.info("Received request to get video metadata by public ID: {}", publicId);

        try {
            VideoMetadataResponseDto metadata = cloudinaryService.getVideoMetadata(publicId);

            log.info("Successfully retrieved metadata for video with public ID: {}", publicId);

            return ApiResponseUtil.success(
                    metadata,
                    "Video metadata retrieved successfully");

        } catch (IOException e) {
            log.error("Failed to retrieve video metadata for public ID: {}", publicId, e);

            // Check if it's a "not found" error
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) {
                return ApiResponseUtil.error(
                        HttpStatus.NOT_FOUND,
                        "Video not found on Cloudinary with public ID: " + publicId);
            }

            return ApiResponseUtil.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve video metadata: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error while retrieving video metadata for public ID: {}", publicId, e);
            return ApiResponseUtil.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error occurred while retrieving video metadata");
        }
    }
}
