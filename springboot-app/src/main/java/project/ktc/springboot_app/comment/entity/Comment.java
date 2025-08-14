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
        @Index(name = "idx_nested_comment_lesson_lft", columnList = "lesson_id,lft"),
        @Index(name = "idx_nested_comment_lft_rgt", columnList = "lft,rgt"),
        @Index(name = "idx_nested_comment_parent_lft", columnList = "parent_id,lft"),
        @Index(name = "idx_nested_comment_user_deleted", columnList = "user_id,is_deleted"),
        @Index(name = "idx_nested_comment_lesson_deleted_lft", columnList = "lesson_id,is_deleted,lft")
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

    // Nested Set Model fields
    @Column(name = "lft", nullable = false)
    private Integer lft;

    @Column(name = "rgt", nullable = false)
    private Integer rgt;

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

    public boolean isLeaf() {
        return rgt - lft == 1;
    }

    public boolean hasChildren() {
        return rgt - lft > 1;
    }

    public int getChildrenCount() {
        return (rgt - lft - 1) / 2;
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

    // Nested Set Model helper methods
    public boolean isAncestorOf(Comment other) {
        return this.lft < other.lft && this.rgt > other.rgt;
    }

    public boolean isDescendantOf(Comment other) {
        return other.isAncestorOf(this);
    }

    public boolean isSibling(Comment other) {
        if (this.parent == null && other.parent == null) {
            return true; // Both are root comments
        }
        if (this.parent == null || other.parent == null) {
            return false;
        }
        return this.parent.getId().equals(other.parent.getId());
    }

    /**
     * Calculate the relative depth from a given ancestor
     */
    public int getRelativeDepth(Comment ancestor) {
        if (!this.isDescendantOf(ancestor)) {
            throw new IllegalArgumentException("This comment is not a descendant of the given ancestor");
        }
        return this.depth - ancestor.depth;
    }

    /**
     * Validate nested set model invariants
     */
    public void validateNestedSetInvariants() {
        if (lft == null || rgt == null) {
            throw new IllegalStateException("Left and right values cannot be null");
        }
        if (lft >= rgt) {
            throw new IllegalStateException("Left value must be less than right value");
        }
        if (lft <= 0 || rgt <= 0) {
            throw new IllegalStateException("Left and right values must be positive");
        }
        if (depth < 0) {
            throw new IllegalStateException("Depth cannot be negative");
        }
    }
}
