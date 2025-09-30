package project.ktc.springboot_app.cache.mappers;

import java.util.List;
import java.util.stream.Collectors;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.review.dto.ReviewResponseDto;
import project.ktc.springboot_app.review.dto.cache.ReviewCacheDto;

/**
 * Utility class for converting between review DTOs and cache DTOs. This mapper
 * ensures clean
 * separation between service layer and cache layer, following the established
 * codebase pattern for
 * cache operations.
 *
 * @author KTC Team
 */
public class ReviewCacheMapper {

	/**
	 * Converts ReviewResponseDto to ReviewCacheDto for Redis storage
	 *
	 * @param responseDto
	 *            The ReviewResponseDto to convert
	 * @return ReviewCacheDto for caching
	 */
	public static ReviewCacheDto toCacheDto(ReviewResponseDto responseDto) {
		if (responseDto == null) {
			return null;
		}

		return ReviewCacheDto.builder()
				.id(responseDto.getId())
				.rating(responseDto.getRating())
				.reviewText(responseDto.getReviewText())
				.reviewedAt(responseDto.getReviewedAt())
				.userId(responseDto.getUser() != null ? responseDto.getUser().getId() : null)
				.userName(responseDto.getUser() != null ? responseDto.getUser().getName() : null)
				.userAvatar(responseDto.getUser() != null ? responseDto.getUser().getAvatar() : null)
				.build();
	}

	/**
	 * Converts ReviewCacheDto back to ReviewResponseDto for service layer usage
	 *
	 * @param cacheDto
	 *            The ReviewCacheDto from cache
	 * @return ReviewResponseDto for service layer
	 */
	public static ReviewResponseDto fromCacheDto(ReviewCacheDto cacheDto) {
		if (cacheDto == null) {
			return null;
		}

		return ReviewResponseDto.builder()
				.id(cacheDto.getId())
				.rating(cacheDto.getRating())
				.reviewText(cacheDto.getReviewText())
				.reviewedAt(cacheDto.getReviewedAt())
				.user(
						ReviewResponseDto.UserSummary.builder()
								.id(cacheDto.getUserId())
								.name(cacheDto.getUserName())
								.avatar(cacheDto.getUserAvatar())
								.build())
				.build();
	}

	/**
	 * Converts list of ReviewResponseDto to list of ReviewCacheDto
	 *
	 * @param responseDtos
	 *            List of ReviewResponseDto to convert
	 * @return List of ReviewCacheDto for caching
	 */
	public static List<ReviewCacheDto> toCacheDtoList(List<ReviewResponseDto> responseDtos) {
		if (responseDtos == null) {
			return null;
		}

		return responseDtos.stream().map(ReviewCacheMapper::toCacheDto).collect(Collectors.toList());
	}

	/**
	 * Converts list of ReviewCacheDto back to list of ReviewResponseDto
	 *
	 * @param cacheDtos
	 *            List of ReviewCacheDto from cache
	 * @return List of ReviewResponseDto for service layer
	 */
	public static List<ReviewResponseDto> fromCacheDtoList(List<ReviewCacheDto> cacheDtos) {
		if (cacheDtos == null) {
			return null;
		}

		return cacheDtos.stream().map(ReviewCacheMapper::fromCacheDto).collect(Collectors.toList());
	}

	/**
	 * Converts PaginatedResponse<ReviewResponseDto> to
	 * PaginatedResponse<ReviewCacheDto> for caching
	 *
	 * @param paginatedResponse
	 *            The paginated response to convert
	 * @return PaginatedResponse with ReviewCacheDto content for caching
	 */
	public static PaginatedResponse<ReviewCacheDto> toCachePaginatedDto(
			PaginatedResponse<ReviewResponseDto> paginatedResponse) {
		if (paginatedResponse == null) {
			return null;
		}

		List<ReviewCacheDto> cacheDtos = toCacheDtoList(paginatedResponse.getContent());

		return PaginatedResponse.<ReviewCacheDto>builder()
				.content(cacheDtos)
				.page(paginatedResponse.getPage()) // Page info remains the same
				.build();
	}

	/**
	 * Converts PaginatedResponse<ReviewCacheDto> back to
	 * PaginatedResponse<ReviewResponseDto>
	 *
	 * @param cachedPaginatedResponse
	 *            The cached paginated response
	 * @return PaginatedResponse with ReviewResponseDto content for service layer
	 */
	public static PaginatedResponse<ReviewResponseDto> fromCachePaginatedDto(
			PaginatedResponse<ReviewCacheDto> cachedPaginatedResponse) {
		if (cachedPaginatedResponse == null) {
			return null;
		}

		List<ReviewResponseDto> responseDtos = fromCacheDtoList(cachedPaginatedResponse.getContent());

		return PaginatedResponse.<ReviewResponseDto>builder()
				.content(responseDtos)
				.page(cachedPaginatedResponse.getPage()) // Page info remains the same
				.build();
	}
}
