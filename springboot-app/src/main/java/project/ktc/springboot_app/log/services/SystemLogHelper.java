package project.ktc.springboot_app.log.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.log.entity.SystemLog;
import project.ktc.springboot_app.log.repository.SystemLogRepository;
import project.ktc.springboot_app.auth.entitiy.User;

/**
 * Helper service for creating system logs with simplified API
 * Focuses on common CRUD operations logging
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SystemLogHelper {

    private final SystemLogRepository systemLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Log a CREATE action
     */
    public void logCreate(User user, String entityType, String entityId, Object newValues) {
        createLog(user, SystemLog.Action.CREATE, entityType, entityId, null, newValues);
    }

    /**
     * Log an UPDATE action
     */
    public void logUpdate(User user, String entityType, String entityId, Object oldValues, Object newValues) {
        createLog(user, SystemLog.Action.UPDATE, entityType, entityId, oldValues, newValues);
    }

    /**
     * Log a DELETE action
     */
    public void logDelete(User user, String entityType, String entityId, Object oldValues) {
        createLog(user, SystemLog.Action.DELETE, entityType, entityId, oldValues, null);
    }

    /**
     * Private method to create and save system log
     */
    private void createLog(User user, SystemLog.Action action, String entityType, String entityId,
            Object oldValues, Object newValues) {
        try {
            SystemLog systemLog = new SystemLog();
            systemLog.setUser(user);
            systemLog.setAction(action);
            systemLog.setEntityType(entityType);
            systemLog.setEntityId(entityId);

            // Convert objects to JSON strings
            if (oldValues != null) {
                systemLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }

            if (newValues != null) {
                systemLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }

            systemLogRepository.save(systemLog);

            log.debug("System log created: {} action for {} entity with ID: {}",
                    action, entityType, entityId);

        } catch (Exception e) {
            log.error("Failed to create system log for {} action on {} entity: {}",
                    action, entityType, e.getMessage());
        }
    }
}
