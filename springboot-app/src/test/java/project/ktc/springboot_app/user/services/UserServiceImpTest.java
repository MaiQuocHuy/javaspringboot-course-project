package project.ktc.springboot_app.user.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import project.ktc.springboot_app.auth.dto.UserResponseDto;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.user.repositories.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImp
 */
class UserServiceImpTest {

    private UserServiceImp userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImp(userRepository);
    }

    @Test
    void testGetProfile_Success() {
        // Arrange
        String userEmail = "test@example.com";
        User mockUser = User.builder()
                .name("Test User")
                .email(userEmail)
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmailWithRoles(userEmail)).thenReturn(Optional.of(mockUser));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(
                SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<ApiResponse<UserResponseDto>> response = userService.getProfile();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ApiResponse<UserResponseDto> body = response.getBody();
            assertNotNull(body);
            assertEquals(200, body.getStatusCode());
            assertEquals("Profile retrieved successfully", body.getMessage());
            assertNotNull(body.getData());
            assertEquals(userEmail, body.getData().getEmail());
        }
    }

    @Test
    void testGetProfile_NotAuthenticated() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(
                SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<ApiResponse<UserResponseDto>> response = userService.getProfile();

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            ApiResponse<UserResponseDto> body = response.getBody();
            assertNotNull(body);
            assertEquals(401, body.getStatusCode());
            assertEquals("User not authenticated", body.getMessage());
        }
    }

    @Test
    void testGetProfile_UserNotAuthenticated() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(
                SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<ApiResponse<UserResponseDto>> response = userService.getProfile();

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            ApiResponse<UserResponseDto> body = response.getBody();
            assertNotNull(body);
            assertEquals(401, body.getStatusCode());
            assertEquals("User not authenticated", body.getMessage());
        }
    }

    @Test
    void testGetProfile_AnonymousUser() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(
                SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<ApiResponse<UserResponseDto>> response = userService.getProfile();

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            ApiResponse<UserResponseDto> body = response.getBody();
            assertNotNull(body);
            assertEquals(401, body.getStatusCode());
            assertEquals("User not authenticated", body.getMessage());
        }
    }

    @Test
    void testGetProfile_UserNotFound() {
        // Arrange
        String userEmail = "notfound@example.com";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmailWithRoles(userEmail)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(
                SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<ApiResponse<UserResponseDto>> response = userService.getProfile();

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            ApiResponse<UserResponseDto> body = response.getBody();
            assertNotNull(body);
            assertEquals(404, body.getStatusCode());
            assertEquals("User not found", body.getMessage());
        }
    }

    @Test
    void testGetProfile_DatabaseException() {
        // Arrange
        String userEmail = "test@example.com";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmailWithRoles(userEmail)).thenThrow(new RuntimeException("Database error"));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(
                SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<ApiResponse<UserResponseDto>> response = userService.getProfile();

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            ApiResponse<UserResponseDto> body = response.getBody();
            assertNotNull(body);
            assertEquals(500, body.getStatusCode());
            assertEquals("Failed to retrieve profile. Please try again later.", body.getMessage());
        }
    }
}
