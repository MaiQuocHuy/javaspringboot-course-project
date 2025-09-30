package project.ktc.springboot_app.auth.entitiy;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import project.ktc.springboot_app.earning.entity.InstructorEarning;
import project.ktc.springboot_app.enrollment.entity.Enrollment;
import project.ktc.springboot_app.entity.BaseEntity;
import project.ktc.springboot_app.entity.RefreshToken;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.entity.VideoContent;
import project.ktc.springboot_app.instructor_application.entity.InstructorApplication;
import project.ktc.springboot_app.payment.entity.Payment;
import project.ktc.springboot_app.review.entity.Review;

@Getter
@Setter
@Builder
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class User extends BaseEntity implements UserDetails {
  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "thumbnail_url")
  @Builder.Default
  private String thumbnailUrl = "";

  @Column(name = "thumbnail_id")
  @Builder.Default
  private String thumbnailId = "";

  @Column(name = "bio")
  @Builder.Default
  private String bio = "";

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id", nullable = false)
  private UserRole role;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<RefreshToken> refreshTokens = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Enrollment> enrollments = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Review> reviews = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Payment> payments = new ArrayList<>();

  @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<InstructorEarning> instructorEarnings = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<InstructorApplication> instructorApplications = new ArrayList<>();

  @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<VideoContent> uploadedVideos = new ArrayList<>();

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    try {
      if (role != null && role.getRole() != null) {
        String roleName = "ROLE_" + role.getRole();
        log.info(" - Loading user authorities for role: {}", roleName);
        return List.of(new SimpleGrantedAuthority(roleName));
      }
    } catch (Exception e) {
      // Log the exception if needed
      System.err.println("Error loading user authorities: " + e.getMessage());
    }
    // Return default STUDENT role if role cannot be loaded
    return List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return isActive;
  }

  @Override
  public String getPassword() {
    return password;
  }
}
