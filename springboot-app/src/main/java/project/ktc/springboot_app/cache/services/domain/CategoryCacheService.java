package project.ktc.springboot_app.cache.services.domain;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.cache.interfaces.CacheService;
import project.ktc.springboot_app.cache.keys.CacheConstants;
import project.ktc.springboot_app.cache.keys.CacheKeyBuilder;
import project.ktc.springboot_app.cache.mappers.CategoryCacheMapper;
import project.ktc.springboot_app.category.dto.CategoryResponseDto;
import project.ktc.springboot_app.category.dto.cache.CategoryCacheDto;

/**
 * Category-specific cache service that provides high-level caching operations for categories.
 * Handles category listing and invalidation using the underlying cache service.
 *
 * @author KTC Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryCacheService {

  private final CacheService cacheService;
  private final CacheKeyBuilder cacheKeyBuilder;

  /**
   * Stores category list in cache
   *
   * @param categories the list of categories to cache
   */
  public void storeCategories(List<CategoryResponseDto> categories) {
    try {
      String cacheKey = cacheKeyBuilder.buildCategoriesListKey();
      log.debug("Caching categories list with key: {}", cacheKey);

      // Convert to cache DTOs using the mapper
      List<CategoryCacheDto> cacheDtos =
          categories.stream().map(CategoryCacheMapper::toCacheDto).collect(Collectors.toList());

      cacheService.store(cacheKey, cacheDtos, CacheConstants.CATEGORIES_TTL);
      log.debug("Successfully cached {} categories", categories.size());

    } catch (Exception e) {
      log.error("Failed to cache categories list", e);
    }
  }

  /**
   * Retrieves category list from cache
   *
   * @return cached category list or null if not found
   */
  public List<CategoryResponseDto> getCategories() {
    try {
      String cacheKey = cacheKeyBuilder.buildCategoriesListKey();
      log.debug("Retrieving categories list from cache with key: {}", cacheKey);

      List<CategoryCacheDto> cacheDtos = cacheService.getList(cacheKey, CategoryCacheDto.class);

      if (cacheDtos != null) {
        log.debug("Cache hit for categories - {} categories found", cacheDtos.size());
        return cacheDtos.stream()
            .map(CategoryCacheMapper::fromCacheDto)
            .collect(Collectors.toList());
      }

      log.debug("Cache miss for categories");
      return null;

    } catch (Exception e) {
      log.error("Failed to retrieve categories from cache", e);
      return null;
    }
  }

  /**
   * Stores specific category in cache
   *
   * @param category the category to cache
   */
  public void storeCategory(CategoryResponseDto category) {
    try {
      String cacheKey = cacheKeyBuilder.buildCategoryDetailKey(category.getId());
      log.debug("Caching category details with key: {}", cacheKey);

      CategoryCacheDto cacheDto = CategoryCacheMapper.toCacheDto(category);
      cacheService.store(cacheKey, cacheDto, CacheConstants.CATEGORIES_TTL);

    } catch (Exception e) {
      log.error("Failed to cache category: {}", category.getId(), e);
    }
  }

  /**
   * Retrieves specific category from cache
   *
   * @param categoryId the category identifier
   * @return cached category or null if not found
   */
  public CategoryResponseDto getCategory(String categoryId) {
    try {
      String cacheKey = cacheKeyBuilder.buildCategoryDetailKey(categoryId);
      log.debug("Retrieving category from cache with key: {}", cacheKey);

      CategoryCacheDto cacheDto = cacheService.get(cacheKey, CategoryCacheDto.class);

      if (cacheDto != null) {
        log.debug("Cache hit for category: {}", categoryId);
        return CategoryCacheMapper.fromCacheDto(cacheDto);
      }

      log.debug("Cache miss for category: {}", categoryId);
      return null;

    } catch (Exception e) {
      log.error("Failed to retrieve category from cache: {}", categoryId, e);
      return null;
    }
  }

  /** Invalidates all category cache entries */
  public void invalidateAllCategories() {
    try {
      log.debug("Invalidating all category cache entries");

      Set<String> keys = cacheService.getKeys(CacheConstants.CATEGORIES_CACHE_PREFIX + ":*");

      if (!keys.isEmpty()) {
        cacheService.remove(keys);
        log.debug("Invalidated {} category cache entries", keys.size());
      } else {
        log.debug("No category cache entries found to invalidate");
      }

    } catch (Exception e) {
      log.error("Failed to invalidate categories cache", e);
    }
  }

  /**
   * Invalidates specific category cache
   *
   * @param categoryId the category identifier
   */
  public void invalidateCategory(String categoryId) {
    try {
      log.debug("Invalidating category cache for: {}", categoryId);

      String cacheKey = cacheKeyBuilder.buildCategoryDetailKey(categoryId);
      cacheService.remove(cacheKey);

      // Also invalidate the categories list cache since it contains this category
      String listKey = cacheKeyBuilder.buildCategoriesListKey();
      cacheService.remove(listKey);

      log.debug("Successfully invalidated category cache for: {}", categoryId);

    } catch (Exception e) {
      log.error("Failed to invalidate category cache for: {}", categoryId, e);
    }
  }

  /**
   * Checks if categories are cached
   *
   * @return true if cached, false otherwise
   */
  public boolean areCategoriesCached() {
    try {
      String cacheKey = cacheKeyBuilder.buildCategoriesListKey();
      return cacheService.exists(cacheKey);

    } catch (Exception e) {
      log.error("Error checking categories cache existence", e);
      return false;
    }
  }

  /**
   * Checks if specific category is cached
   *
   * @param categoryId the category identifier
   * @return true if cached, false otherwise
   */
  public boolean isCategoryCached(String categoryId) {
    try {
      String cacheKey = cacheKeyBuilder.buildCategoryDetailKey(categoryId);
      return cacheService.exists(cacheKey);

    } catch (Exception e) {
      log.error("Error checking category cache existence for: {}", categoryId, e);
      return false;
    }
  }

  /** Get cache statistics for categories */
  public void logCacheStatistics() {
    try {
      Set<String> keys = cacheService.getKeys(CacheConstants.CATEGORIES_CACHE_PREFIX + ":*");
      log.info("Categories cache contains {} entries", keys.size());

    } catch (Exception e) {
      log.error("Failed to get categories cache statistics", e);
    }
  }

  // ==================== Compatibility Methods ====================

  /** Alias method for getCategoriesList - for compatibility */
  public List<CategoryResponseDto> getCategoriesList() {
    return getCategories();
  }

  /** Alias method for storeCategories - for compatibility */
  public void storeCategoriesList(List<CategoryResponseDto> categories) {
    storeCategories(categories);
  }

  /** Alias method for invalidateAllCategories - for compatibility */
  public void invalidateAllCategoriesCache() {
    invalidateAllCategories();
  }
}
