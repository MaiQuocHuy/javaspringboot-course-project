package project.ktc.springboot_app.upload.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.upload.dto.ImageUploadResponseDto;
import project.ktc.springboot_app.upload.service.CloudinaryServiceImp;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * REST Controller for handling file uploads
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Upload API", description = "Endpoints for uploading and managing files")
public class UploadController {

    private final CloudinaryServiceImp cloudinaryService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload an image file", description = "Upload an image file to Cloudinary and return the secure URL. Supported formats: JPEG, PNG, GIF, BMP, WebP. Maximum size: 10MB.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Image uploaded successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ImageUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file format or file too large", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Upload failed due to server error", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<ImageUploadResponseDto>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        log.info("Received image upload request: {} ({})",
                file.getOriginalFilename(), file.getContentType());

        ImageUploadResponseDto result = cloudinaryService.uploadImage(file);

        return ApiResponseUtil.created(result, "Image uploaded successfully");
    }

    @DeleteMapping("/image/**")
    @Operation(summary = "Delete an image", description = "Delete an image from Cloudinary using its public ID. The public ID should be included in the URL path after /image/. For example: /image/course-management/filename_timestamp_uuid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found"),
            @ApiResponse(responseCode = "500", description = "Deletion failed due to server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> deleteImage(
            HttpServletRequest request ) {

        // Extract the public ID from the URL path (everything after /api/upload/image/)
        String fullPath = request.getRequestURI();
        String publicId = fullPath.substring("/api/upload/image/".length());

        // URL decode the public ID to handle encoded characters like %2F -> /
        publicId = URLDecoder.decode(publicId, StandardCharsets.UTF_8);

        log.info("Received image deletion request for public ID: {}", publicId);

        boolean deleted = cloudinaryService.deleteImage(publicId);

        if (deleted) {
            return ApiResponseUtil.success("Image deleted successfully");
        } else {
            return ApiResponseUtil.notFound("Image not found or already deleted");
        }
    }
}
