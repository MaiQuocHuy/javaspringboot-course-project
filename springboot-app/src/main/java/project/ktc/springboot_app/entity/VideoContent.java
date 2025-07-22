package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "VIDEO_CONTENT")
@Getter
@Setter
public class VideoContent extends BaseEntity {
    @Column(columnDefinition = "text", nullable = false)
    private String url;

    @Column
    private Integer duration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 