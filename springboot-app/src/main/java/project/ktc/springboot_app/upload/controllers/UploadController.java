package project.ktc.springboot_app.upload.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import project.ktc.springboot_app.upload.dto.VideoUploadResponseDto;
import project.ktc.springboot_app.upload.exception.InvalidImageFormatException;
import project.ktc.springboot_app.upload.exception.InvalidVideoFormatException;
import project.ktc.springboot_app.upload.services.CloudinaryServiceImp;
import project.ktc.springboot_app.upload.services.FileValidationService;

/** REST Controller for handling file uploads */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Upload API", description = "Endpoints for uploading and managing files")
public class UploadController {

	private final CloudinaryServiceImp cloudinaryService;
	private final FileValidationService fileValidationService;

	@PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Upload an image file", description = """
			Upload an image file to Cloudinary and return the secure URL.

			**Validation Rules:**
			- File must not be empty
			- Supported MIME types: image/jpeg, image/png, image/gif, image/bmp, image/webp
			- Maximum file size: 10MB

			**Returns:** Secure URL, public ID, and file metadata on successful upload.
			""")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Image uploaded successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ImageUploadResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "Validation failed - invalid file format, empty file, or file too large", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "500", description = "Upload failed due to server error", content = @Content(mediaType = "application/json"))
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<ImageUploadResponseDto>> uploadImage(
			@RequestParam("file") MultipartFile file) {

		log.info(
				"Received image upload request: {} ({})",
				file.getOriginalFilename(),
				file.getContentType());

		try {
			// Validate the uploaded file
			fileValidationService.validateImageFile(file);

			// Upload to Cloudinary if validation passes
			ImageUploadResponseDto result = cloudinaryService.uploadImage(file);

			return ApiResponseUtil.created(result, "Image uploaded successfully");

		} catch (InvalidImageFormatException e) {
			log.warn("File validation failed for {}: {}", file.getOriginalFilename(), e.getMessage());
			return ApiResponseUtil.badRequest(e.getMessage());
		}
	}

	@PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Upload a video file", description = """
			Upload a video file to Cloudinary and return the secure URL with metadata.

			**Validation Rules:**
			- File must not be empty
			- Supported MIME types: video/mp4, video/mpeg, video/quicktime, video/x-msvideo (AVI), video/x-ms-wmv (WMV), video/webm, video/ogg
			- Maximum file size: 100MB
			- Video will be automatically converted to MP4 format for compatibility

			**Returns:** Secure URL, public ID, video metadata (duration, dimensions), and upload details.
			""")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Video uploaded successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VideoUploadResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "Validation failed - invalid file format, empty file, or file too large", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "500", description = "Upload failed due to server error", content = @Content(mediaType = "application/json"))
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<VideoUploadResponseDto>> uploadVideo(
			@Parameter(description = "Video file to upload (max 100MB)", required = true) @RequestParam("file") MultipartFile file) {

		log.info(
				"Received video upload request: {} ({}, {} MB)",
				file.getOriginalFilename(),
				file.getContentType(),
				file.getSize() / (1024 * 1024));

		try {
			// Validate the uploaded video file
			fileValidationService.validateVideoFile(file);

			// Upload to Cloudinary if validation passes
			VideoUploadResponseDto result = cloudinaryService.uploadVideo(file);

			return ApiResponseUtil.created(result, "Video uploaded successfully");

		} catch (InvalidVideoFormatException e) {
			log.warn("Video validation failed for {}: {}", file.getOriginalFilename(), e.getMessage());
			return ApiResponseUtil.badRequest(e.getMessage());
		}
	}

	@DeleteMapping("/image/{publicId}")
	@Operation(summary = "Delete an image", description = """
			Delete an image from Cloudinary using its public ID.
			Use `%2F` for slashes in nested folder names.
			Example: `course-management%2Fimage_timestamp_uuid`
			""")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Image deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Image not found"),
			@ApiResponse(responseCode = "500", description = "Server error during deletion")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> deleteImage(
			@Parameter(description = "Cloudinary public ID, e.g. `course-management%2Fimage_timestamp_uuid`", required = true) @PathVariable String publicId) {
		log.info("Received image deletion request for public ID: {}", publicId);
		// Decode public ID to handle URL encoding
		String decodedPublicId = "course-management/" + publicId;
		boolean deleted = cloudinaryService.deleteImage(decodedPublicId);
		log.info("Image deletion result for public ID {}: {}", decodedPublicId, deleted);
		return deleted
				? ApiResponseUtil.success("Image deleted successfully")
				: ApiResponseUtil.notFound("Image not found or already deleted");
	}

	@DeleteMapping("/video/{publicId}")
	@Operation(summary = "Delete a video", description = """
			Delete a video from Cloudinary using its public ID.
			Use `%2F` for slashes in nested folder names.
			Example: `course-videos%2Fvideo_timestamp_uuid`
			""")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Video deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Video not found"),
			@ApiResponse(responseCode = "500", description = "Server error during deletion")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> deleteVideo(
			@Parameter(description = "Cloudinary public ID for video, e.g. `course-videos%2Fvideo_timestamp_uuid`", required = true) @PathVariable String publicId) {

		log.info("Received video deletion request for public ID: {}", publicId);

		// Decode public ID to handle URL encoding
		String decodedPublicId = "course-videos/" + publicId;
		boolean deleted = cloudinaryService.deleteVideo(decodedPublicId);

		log.info("Video deletion result for public ID {}: {}", decodedPublicId, deleted);

		return deleted
				? ApiResponseUtil.success("Video deleted successfully")
				: ApiResponseUtil.notFound("Video not found or already deleted");
	}
}
