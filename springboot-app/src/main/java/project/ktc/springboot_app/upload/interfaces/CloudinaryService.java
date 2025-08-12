package project.ktc.springboot_app.upload.interfaces;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import project.ktc.springboot_app.upload.dto.DocumentUploadResponseDto;
import project.ktc.springboot_app.upload.dto.ImageUploadResponseDto;
import project.ktc.springboot_app.upload.dto.VideoMetadataResponseDto;
import project.ktc.springboot_app.upload.dto.VideoUploadResponseDto;

public interface CloudinaryService {
    ImageUploadResponseDto uploadImage(MultipartFile file) throws IOException;

    VideoUploadResponseDto uploadVideo(MultipartFile file) throws IOException;

    DocumentUploadResponseDto uploadDocument(MultipartFile file) throws IOException;

    boolean deleteImage(String publicId) throws IOException;

    boolean deleteVideo(String publicId) throws IOException;

    boolean deleteDocument(String publicId) throws IOException;

    /**
     * Retrieve video metadata from Cloudinary video URL or public ID
     * 
     * @param videoUrlOrPublicId Cloudinary video URL or public ID
     * @return VideoMetadataResponseDto containing video metadata
     * @throws IOException if metadata retrieval fails
     */
    VideoMetadataResponseDto getVideoMetadata(String videoUrlOrPublicId) throws IOException;
}
