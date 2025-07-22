package project.ktc.springboot_app.upload.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.upload.exception.InvalidImageFormatException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FileValidationService
 */
class FileValidationServiceTest {

    private FileValidationService fileValidationService;

    @Mock
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fileValidationService = new FileValidationService();
    }

    @Test
    void testValidateImageFile_ValidFile_ShouldPass() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(5 * 1024 * 1024L); // 5MB
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");

        // Act & Assert
        assertDoesNotThrow(() -> fileValidationService.validateImageFile(mockFile));
    }

    @Test
    void testValidateImageFile_EmptyFile_ShouldThrowException() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(true);

        // Act & Assert
        InvalidImageFormatException exception = assertThrows(
                InvalidImageFormatException.class,
                () -> fileValidationService.validateImageFile(mockFile));
        assertEquals("File is empty or not provided", exception.getMessage());
    }

    @Test
    void testValidateImageFile_FileTooLarge_ShouldThrowException() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(15 * 1024 * 1024L); // 15MB
        when(mockFile.getOriginalFilename()).thenReturn("large-file.jpg");

        // Act & Assert
        InvalidImageFormatException exception = assertThrows(
                InvalidImageFormatException.class,
                () -> fileValidationService.validateImageFile(mockFile));
        assertTrue(exception.getMessage().contains("File size (15 MB) exceeds maximum allowed size of 10 MB"));
    }

    @Test
    void testValidateImageFile_InvalidMimeType_ShouldThrowException() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(5 * 1024 * 1024L);
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getOriginalFilename()).thenReturn("document.pdf");

        // Act & Assert
        InvalidImageFormatException exception = assertThrows(
                InvalidImageFormatException.class,
                () -> fileValidationService.validateImageFile(mockFile));
        assertTrue(exception.getMessage().contains("Invalid file format 'application/pdf'"));
    }

    @Test
    void testValidateImageFile_NullContentType_ShouldThrowException() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(5 * 1024 * 1024L);
        when(mockFile.getContentType()).thenReturn(null);
        when(mockFile.getOriginalFilename()).thenReturn("unknown-file");

        // Act & Assert
        InvalidImageFormatException exception = assertThrows(
                InvalidImageFormatException.class,
                () -> fileValidationService.validateImageFile(mockFile));
        assertEquals("File content type could not be determined", exception.getMessage());
    }

    @Test
    void testGetAllowedMimeTypes() {
        // Act
        var allowedTypes = fileValidationService.getAllowedMimeTypes();

        // Assert
        assertEquals(5, allowedTypes.size());
        assertTrue(allowedTypes.contains("image/jpeg"));
        assertTrue(allowedTypes.contains("image/png"));
        assertTrue(allowedTypes.contains("image/gif"));
        assertTrue(allowedTypes.contains("image/bmp"));
        assertTrue(allowedTypes.contains("image/webp"));
    }

    @Test
    void testGetMaxFileSizeMB() {
        // Act
        long maxSize = fileValidationService.getMaxFileSizeMB();

        // Assert
        assertEquals(10L, maxSize);
    }
}
