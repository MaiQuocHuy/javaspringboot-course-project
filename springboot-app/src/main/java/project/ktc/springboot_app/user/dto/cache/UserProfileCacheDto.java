package project.ktc.springboot_app.user.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Cache DTO for User Profile data to avoid caching ResponseEntity objects.
 * This DTO contains only the essential user profile information needed for
 * caching.
 * 
 * @author KTC Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileCacheDto implements Serializable {

    /**
     * User unique identifier
     */
    private String id;

    /**
     * User email address
     */
    private String email;

    /**
     * User full name
     */
    private String name;

    /**
     * User role (STUDENT, INSTRUCTOR, ADMIN)
     */
    private String role;

    /**
     * User profile picture URL
     */
    private String thumbnailUrl;

    /**
     * User profile picture ID
     */
    private String thumbnailId;

    /**
     * User biography or description
     */
    private String bio;

    /**
     * Indicates if the user is active
     */
    private Boolean isActive;
}