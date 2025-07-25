package project.ktc.springboot_app.upload.interfaces;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import project.ktc.springboot_app.upload.dto.ImageUploadResponseDto;
import project.ktc.springboot_app.upload.dto.VideoUploadResponseDto;

public interface CloudinaryService {
    ImageUploadResponseDto uploadImage(MultipartFile file) throws IOException;

    VideoUploadResponseDto uploadVideo(MultipartFile file) throws IOException;

    boolean deleteImage(String publicId) throws IOException;

    boolean deleteVideo(String publicId) throws IOException;
}
