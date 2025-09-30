package project.ktc.springboot_app.cache.mappers;

import java.util.List;
import java.util.stream.Collectors;
import project.ktc.springboot_app.category.dto.CategoryResponseDto;
import project.ktc.springboot_app.category.dto.cache.CategoryCacheDto;

/**
 * Utility class for converting between category DTOs and cache DTOs. This mapper ensures clean
 * separation between service layer and cache layer.
 *
 * @author KTC Team
 */
public class CategoryCacheMapper {

  /**
   * Converts CategoryResponseDto to CategoryCacheDto for Redis storage
   *
   * @param responseDto The CategoryResponseDto to convert
   * @return CategoryCacheDto for caching
   */
  public static CategoryCacheDto toCacheDto(CategoryResponseDto responseDto) {
    if (responseDto == null) {
      return null;
    }

    return CategoryCacheDto.builder()
        .id(responseDto.getId())
        .name(responseDto.getName())
        .description(responseDto.getDescription())
        .slug(responseDto.getSlug())
        .courseCount(responseDto.getCourseCount())
        .build();
  }

  /**
   * Converts CategoryCacheDto back to CategoryResponseDto for service layer usage
   *
   * @param cacheDto The CategoryCacheDto from cache
   * @return CategoryResponseDto for service layer
   */
  public static CategoryResponseDto fromCacheDto(CategoryCacheDto cacheDto) {
    if (cacheDto == null) {
      return null;
    }

    return CategoryResponseDto.builder()
        .id(cacheDto.getId())
        .name(cacheDto.getName())
        .description(cacheDto.getDescription())
        .slug(cacheDto.getSlug())
        .courseCount(cacheDto.getCourseCount())
        .build();
  }

  /**
   * Converts a list of CategoryResponseDto to a list of CategoryCacheDto
   *
   * @param responseDtos List of CategoryResponseDto to convert
   * @return List of CategoryCacheDto for caching
   */
  public static List<CategoryCacheDto> toCacheDtoList(List<CategoryResponseDto> responseDtos) {
    if (responseDtos == null) {
      return null;
    }

    return responseDtos.stream().map(CategoryCacheMapper::toCacheDto).collect(Collectors.toList());
  }

  /**
   * Converts a list of CategoryCacheDto to a list of CategoryResponseDto
   *
   * @param cacheDtos List of CategoryCacheDto from cache
   * @return List of CategoryResponseDto for service layer
   */
  public static List<CategoryResponseDto> fromCacheDtoList(List<CategoryCacheDto> cacheDtos) {
    if (cacheDtos == null) {
      return null;
    }

    return cacheDtos.stream().map(CategoryCacheMapper::fromCacheDto).collect(Collectors.toList());
  }
}
