package project.ktc.springboot_app.upload.service;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.upload.dto.ImageUploadResponseDto;
import project.ktc.springboot_app.upload.exception.ImageUploadException;
import project.ktc.springboot_app.upload.exception.InvalidImageFormatException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CloudinaryService
 */
@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private com.cloudinary.Uploader uploader;

    @InjectMocks
    private CloudinaryServiceImp cloudinaryService;

    @BeforeEach
    void setUp() {
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    void uploadImage_Success() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes());

        Map<String, Object> mockResponse = Map.of(
                "secure_url", "https://res.cloudinary.com/test/image/upload/test.jpg",
                "public_id", "course-management/test_123456789_abcd1234",
                "format", "jpg",
                "width", 800,
                "height", 600);

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockResponse);

        // When
        ImageUploadResponseDto result = cloudinaryService.uploadImage(file);

        // Then
        assertNotNull(result);
        assertEquals("https://res.cloudinary.com/test/image/upload/test.jpg", result.getUrl());
        assertEquals("course-management/test_123456789_abcd1234", result.getPublicId());
        assertEquals("test.jpg", result.getOriginalFilename());
        assertEquals("jpg", result.getFormat());
        assertEquals(800, result.getWidth());
        assertEquals(600, result.getHeight());

        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadImage_EmptyFile_ThrowsException() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]);

        // When & Then
        InvalidImageFormatException exception = assertThrows(
                InvalidImageFormatException.class,
                () -> cloudinaryService.uploadImage(emptyFile));

        assertEquals("File is empty", exception.getMessage());
        verifyNoInteractions(uploader);
    }

    @Test
    void uploadImage_InvalidFormat_ThrowsException() {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes());

        // When & Then
        InvalidImageFormatException exception = assertThrows(
                InvalidImageFormatException.class,
                () -> cloudinaryService.uploadImage(file));

        assertTrue(exception.getMessage().contains("Invalid file format"));
        verifyNoInteractions(uploader);
    }

    @Test
    void uploadImage_UploadFails_ThrowsException() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes());

        when(uploader.upload(any(byte[].class), anyMap()))
                .thenThrow(new RuntimeException("Cloudinary error"));

        // When & Then
        ImageUploadException exception = assertThrows(
                ImageUploadException.class,
                () -> cloudinaryService.uploadImage(file));

        assertTrue(exception.getMessage().contains("Unexpected error occurred during upload"));
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void deleteImage_Success() throws Exception {
        // Given
        String publicId = "test-public-id";
        Map<String, Object> mockResponse = Map.of("result", "ok");

        when(uploader.destroy(eq(publicId), anyMap())).thenReturn(mockResponse);

        // When
        boolean result = cloudinaryService.deleteImage(publicId);

        // Then
        assertTrue(result);
        verify(uploader).destroy(eq(publicId), anyMap());
    }

    @Test
    void deleteImage_NotFound() throws Exception {
        // Given
        String publicId = "non-existent-id";
        Map<String, Object> mockResponse = Map.of("result", "not found");

        when(uploader.destroy(eq(publicId), anyMap())).thenReturn(mockResponse);

        // When
        boolean result = cloudinaryService.deleteImage(publicId);

        // Then
        assertFalse(result);
        verify(uploader).destroy(eq(publicId), anyMap());
    }
}
