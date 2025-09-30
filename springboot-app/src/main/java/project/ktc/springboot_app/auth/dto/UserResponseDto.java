package project.ktc.springboot_app.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {
  private String id;
  private String email;
  private String name;
  private String role; // Single role instead of list
  private String thumbnailUrl; // URL to user profile picture
  private String thumbnailId; // ID of the thumbnail image
  private String bio; // Short biography or description
  private Boolean isActive; // Indicates if the user is active

  public UserResponseDto(User user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.name = user.getName();
    this.thumbnailUrl = user.getThumbnailUrl();
    this.thumbnailId = user.getThumbnailId();
    this.bio = user.getBio();
    this.isActive = user.getIsActive();
    if (user.getRole() != null && user.getRole().getRole() != null) {
      this.role = user.getRole().getRole().toUpperCase().trim();
    }
  }
}
