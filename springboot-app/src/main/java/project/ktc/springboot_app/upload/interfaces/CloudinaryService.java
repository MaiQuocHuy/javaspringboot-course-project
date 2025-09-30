package project.ktc.springboot_app.upload.interfaces;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.upload.dto.AudioUploadResponseDto;
import project.ktc.springboot_app.upload.dto.DocumentUploadResponseDto;
import project.ktc.springboot_app.upload.dto.ImageUploadResponseDto;
import project.ktc.springboot_app.upload.dto.VideoMetadataResponseDto;
import project.ktc.springboot_app.upload.dto.VideoUploadResponseDto;

public interface CloudinaryService {
  ImageUploadResponseDto uploadImage(MultipartFile file) throws IOException;

  VideoUploadResponseDto uploadVideo(MultipartFile file) throws IOException;

  AudioUploadResponseDto uploadAudio(MultipartFile file) throws IOException;

  DocumentUploadResponseDto uploadDocument(MultipartFile file) throws IOException;

  boolean deleteImage(String publicId) throws IOException;

  boolean deleteVideo(String publicId) throws IOException;

  boolean deleteAudio(String publicId) throws IOException;

  boolean deleteDocument(String publicId) throws IOException;

  /**
   * Retrieve video metadata from Cloudinary video URL or public ID
   *
   * @param videoUrlOrPublicId Cloudinary video URL or public ID
   * @return VideoMetadataResponseDto containing video metadata
   * @throws IOException if metadata retrieval fails
   */
  VideoMetadataResponseDto getVideoMetadata(String videoUrlOrPublicId) throws IOException;

  /**
   * Upload PDF certificate to Cloudinary
   *
   * @param pdfData PDF file as byte array
   * @param filename Name of the PDF file
   * @return Document upload response with URL
   * @throws IOException if upload fails
   */
  DocumentUploadResponseDto uploadCertificatePdf(byte[] pdfData, String filename)
      throws IOException;

  /**
   * Upload certificate image to Cloudinary
   *
   * @param imageData Image file as byte array
   * @param filename Name of the image file
   * @return Image upload response with URL
   * @throws IOException if upload fails
   */
  ImageUploadResponseDto uploadCertificateImage(byte[] imageData, String filename)
      throws IOException;
}
