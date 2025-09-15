package project.ktc.springboot_app.cache.services.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import project.ktc.springboot_app.auth.dto.UserResponseDto;
import project.ktc.springboot_app.cache.interfaces.CacheService;
import project.ktc.springboot_app.cache.keys.CacheConstants;
import project.ktc.springboot_app.cache.keys.CacheKeyBuilder;
import project.ktc.springboot_app.cache.mappers.UserProfileCacheMapper;
import project.ktc.springboot_app.user.dto.cache.UserProfileCacheDto;

/**
 * User-specific cache service that provides high-level caching operations
 * for user profiles.
 * Handles user profile caching, retrieval, and invalidation using the
 * underlying cache service.
 * 
 * @author KTC Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserCacheService {

    private final CacheService cacheService;
    private final CacheKeyBuilder cacheKeyBuilder;

    /**
     * Stores user profile in cache
     * 
     * @param userProfile the user profile to cache
     */
    public void storeUserProfile(UserResponseDto userProfile) {
        try {
            String cacheKey = cacheKeyBuilder.buildUserProfileDetailKey(userProfile.getEmail());
            log.debug("Caching user profile with key: {}", cacheKey);

            // Convert to cache DTO using the mapper
            UserProfileCacheDto cacheDto = UserProfileCacheMapper.toCacheDto(userProfile);

            cacheService.store(cacheKey, cacheDto, CacheConstants.USER_PROFILE_DETAIL_TTL);
            log.debug("Successfully cached user profile for user: {}", userProfile.getEmail());

        } catch (Exception e) {
            log.error("Failed to cache user profile: {}", userProfile.getEmail(), e);
        }
    }

    /**
     * Retrieves user profile from cache
     * 
     * @param userEmail user email identifier
     * @return cached user profile or null if not found
     */
    public UserResponseDto getUserProfile(String userEmail) {
        try {
            String cacheKey = cacheKeyBuilder.buildUserProfileDetailKey(userEmail);
            log.debug("Retrieving user profile from cache with key: {}", cacheKey);

            UserProfileCacheDto cacheDto = (UserProfileCacheDto) cacheService.get(cacheKey);

            if (cacheDto != null) {
                log.debug("Cache hit for user profile: {}", userEmail);
                return UserProfileCacheMapper.fromCacheDto(cacheDto);
            }

            log.debug("Cache miss for user profile: {}", userEmail);
            return null;

        } catch (Exception e) {
            log.error("Failed to retrieve user profile from cache: {}", userEmail, e);
            return null;
        }
    }

    /**
     * Invalidates user profile cache for a specific user
     * 
     * @param userEmail user email identifier
     */
    public void invalidateUserProfile(String userEmail) {
        try {
            String cacheKey = cacheKeyBuilder.buildUserProfileDetailKey(userEmail);
            log.debug("Invalidating user profile cache with key: {}", cacheKey);

            cacheService.remove(cacheKey);
            log.debug("Successfully invalidated user profile cache for user: {}", userEmail);

        } catch (Exception e) {
            log.error("Failed to invalidate user profile cache: {}", userEmail, e);
        }
    }

    /**
     * Checks if user profile exists in cache
     * 
     * @param userEmail user email identifier
     * @return true if user profile is cached, false otherwise
     */
    public boolean isUserProfileCached(String userEmail) {
        try {
            String cacheKey = cacheKeyBuilder.buildUserProfileDetailKey(userEmail);
            return cacheService.exists(cacheKey);
        } catch (Exception e) {
            log.error("Failed to check if user profile is cached: {}", userEmail, e);
            return false;
        }
    }
}