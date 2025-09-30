package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_roles")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserRole {
  @Id
  @Column(length = 36, nullable = false)
  @Builder.Default
  private String id = java.util.UUID.randomUUID().toString();

  @Column(length = 36, nullable = false)
  private String role;
}
