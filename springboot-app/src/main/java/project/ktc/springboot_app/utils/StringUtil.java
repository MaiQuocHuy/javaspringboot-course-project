package project.ktc.springboot_app.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class StringUtil {

  /**
   * Generates a normalized slug from a given name This ensures consistent slug format across the
   * application
   *
   * @param name the name to convert to slug
   * @return normalized slug, empty string if input is null/empty
   */
  public String generateSlug(String name) {
    if (name == null || name.trim().isEmpty()) {
      return "";
    }

    return name.trim()
        .toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters except spaces and hyphens
        .replaceAll("\\s+", "-") // Replace spaces with hyphens
        .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
        .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
  }

  /**
   * Normalizes a slug for comparison purposes This method should be used when checking for
   * duplicate slugs
   *
   * @param slug the slug to normalize
   * @return normalized slug for comparison, empty string if input is null/empty
   */
  public String normalizeSlugForComparison(String slug) {
    if (slug == null || slug.trim().isEmpty()) {
      return "";
    }

    return slug.trim().toLowerCase();
  }

  public String getBeforeDot(String input) {
    if (input == null) return null;
    int dotIndex = input.indexOf('.');
    if (dotIndex != -1) {
      return input.substring(0, dotIndex);
    }
    return input; // Không có dấu .
  }
}
