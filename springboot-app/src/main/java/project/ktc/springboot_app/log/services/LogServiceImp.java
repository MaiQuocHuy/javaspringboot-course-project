package project.ktc.springboot_app.log.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.log.dto.CreateLogDto;
import project.ktc.springboot_app.log.dto.LogResponseDto;
import project.ktc.springboot_app.log.entity.SystemLog;
import project.ktc.springboot_app.log.interfaces.LogService;
import project.ktc.springboot_app.log.repository.SystemLogRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;

/**
 * Service implementation for system log operations
 * Following SOLID principles: SRP, DIP, OCP
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogServiceImp implements LogService {

    private final SystemLogRepository systemLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<LogResponseDto>> createLog(CreateLogDto createLogDto) {
        try {
            log.info("Creating system log entry for user: {}, action: {}, entity: {}",
                    createLogDto.getUserId(), createLogDto.getAction(), createLogDto.getEntityType());

            // Validate user exists
            User user = userRepository.findById(createLogDto.getUserId()).orElse(null);
            if (user == null) {
                log.warn("User not found with ID: {}", createLogDto.getUserId());
                return ApiResponseUtil.badRequest("User not found with the provided ID");
            }

            // Validate JSON format for oldValues and newValues if provided
            if (!isValidJson(createLogDto.getOldValues())) {
                log.warn("Invalid JSON format for oldValues");
                return ApiResponseUtil.badRequest("Invalid JSON format for oldValues");
            }

            if (!isValidJson(createLogDto.getNewValues())) {
                log.warn("Invalid JSON format for newValues");
                return ApiResponseUtil.badRequest("Invalid JSON format for newValues");
            }

            // Create and save the system log
            SystemLog systemLog = new SystemLog();
            systemLog.setUser(user);
            systemLog.setAction(createLogDto.getAction());
            systemLog.setEntityType(createLogDto.getEntityType());
            systemLog.setEntityId(createLogDto.getEntityId());

            systemLog.setOldValues(createLogDto.getOldValues());
            systemLog.setNewValues(createLogDto.getNewValues());

            SystemLog savedLog = systemLogRepository.save(systemLog);

            log.info("System log created successfully with ID: {}", savedLog.getId());

            // Create response DTO
            LogResponseDto responseDto = LogResponseDto.builder()
                    .id(savedLog.getId())
                    .build();

            return ApiResponseUtil.created(responseDto, "Log entry created successfully");

        } catch (Exception e) {
            log.error("Error creating system log: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to create log entry. Please try again later.");
        }
    }

    /**
     * Validates if a string is valid JSON or null
     * 
     * @param json The JSON string to validate
     * @return true if valid or null, false otherwise
     */
    private boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return true; // null or empty is valid
        }

        try {
            // Basic JSON validation - starts with { or [
            json = json.trim();
            return (json.startsWith("{") && json.endsWith("}")) ||
                    (json.startsWith("[") && json.endsWith("]"));
        } catch (Exception e) {
            return false;
        }
    }
}
