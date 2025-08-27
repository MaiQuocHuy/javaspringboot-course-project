package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;

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

    @Column(length = 500, nullable = true)
    private String description;

}