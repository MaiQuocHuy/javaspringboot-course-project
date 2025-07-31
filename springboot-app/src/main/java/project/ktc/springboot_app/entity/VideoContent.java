package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;

@Entity
@Table(name = "video_contents")
@Getter
@Setter
public class VideoContent extends BaseEntity {
    @Column(columnDefinition = "TEXT", nullable = false)
    private String url;

    @Column
    private Integer duration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;
}