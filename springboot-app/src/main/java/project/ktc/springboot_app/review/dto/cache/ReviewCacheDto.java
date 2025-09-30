package project.ktc.springboot_app.review.dto.cache;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cache DTO for storing review data in Redis. This DTO provides clean separation between service
 * layer and cache layer, containing only essential review information needed for caching.
 *
 * @author KTC Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCacheDto {

  private String id;
  private Integer rating;
  private String reviewText;
  private LocalDateTime reviewedAt;

  // User information (flattened to avoid circular references)
  private String userId;
  private String userName;
  private String userAvatar;

  /** Nested DTO for user summary information in cached reviews */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserSummaryCacheDto {
    private String id;
    private String name;
    private String avatar;
  }
}
