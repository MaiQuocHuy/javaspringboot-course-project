package project.ktc.springboot_app.comment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.ktc.springboot_app.entity.BaseEntity;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.lesson.entity.Lesson;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_lesson_created", columnList = "lesson_id,created_at"),
        @Index(name = "idx_comment_parent", columnList = "parent_id"),
        @Index(name = "idx_comment_user_deleted", columnList = "user_id,is_deleted")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private Integer depth = 0;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "is_edited", nullable = false)
    @Builder.Default
    private Boolean isEdited = false;

    @Version
    private Long version; // For optimistic locking

    // Business methods
    public boolean isReply() {
        return parent != null;
    }

    public boolean isRootComment() {
        return parent == null;
    }

    public boolean canAddReply() {
        return depth < 2; // Max 3 levels (0, 1, 2)
    }

    public void markAsDeleted() {
        this.isDeleted = true;
        this.content = "[Deleted]";
    }

    public void updateContent(String newContent) {
        if (!this.content.equals(newContent)) {
            this.content = newContent;
            this.isEdited = true;
        }
    }

    public boolean isOwnedBy(String userId) {
        return this.user.getId().equals(userId);
    }
}
