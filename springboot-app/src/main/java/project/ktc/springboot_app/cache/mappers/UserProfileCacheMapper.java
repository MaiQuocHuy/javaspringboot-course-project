package project.ktc.springboot_app.cache.mappers;

import project.ktc.springboot_app.auth.dto.UserResponseDto;
import project.ktc.springboot_app.user.dto.cache.UserProfileCacheDto;

/**
 * Utility class for converting between user profile DTOs and cache DTOs. This
 * mapper ensures clean
 * separation between service layer and cache layer.
 *
 * @author KTC Team
 */
public class UserProfileCacheMapper {

	/**
	 * Converts UserResponseDto to UserProfileCacheDto for Redis storage
	 *
	 * @param responseDto
	 *            The UserResponseDto to convert
	 * @return UserProfileCacheDto for caching
	 */
	public static UserProfileCacheDto toCacheDto(UserResponseDto responseDto) {
		if (responseDto == null) {
			return null;
		}

		return UserProfileCacheDto.builder()
				.id(responseDto.getId())
				.email(responseDto.getEmail())
				.name(responseDto.getName())
				.role(responseDto.getRole())
				.thumbnailUrl(responseDto.getThumbnailUrl())
				.thumbnailId(responseDto.getThumbnailId())
				.bio(responseDto.getBio())
				.isActive(responseDto.getIsActive())
				.build();
	}

	/**
	 * Converts UserProfileCacheDto back to UserResponseDto for service layer usage
	 *
	 * @param cacheDto
	 *            The UserProfileCacheDto from cache
	 * @return UserResponseDto for service layer
	 */
	public static UserResponseDto fromCacheDto(UserProfileCacheDto cacheDto) {
		if (cacheDto == null) {
			return null;
		}

		return new UserResponseDto(
				cacheDto.getId(),
				cacheDto.getEmail(),
				cacheDto.getName(),
				cacheDto.getRole(),
				cacheDto.getThumbnailUrl(),
				cacheDto.getThumbnailId(),
				cacheDto.getBio(),
				cacheDto.getIsActive());
	}
}
