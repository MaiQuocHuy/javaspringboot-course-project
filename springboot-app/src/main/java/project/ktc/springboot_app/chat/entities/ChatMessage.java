package project.ktc.springboot_app.chat.entities;

import jakarta.persistence.*;
import lombok.*;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.entity.BaseEntity;
import project.ktc.springboot_app.auth.entitiy.User;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_msg_course_created", columnList = "course_id, created_at"),
        @Index(name = "idx_chat_msg_sender", columnList = "sender_id"),
        @Index(name = "idx_chat_msg_type", columnList = "message_type_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "sender_role", nullable = false, length = 30)
    private String senderRole; // Keep enum value as stored

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_type_id", nullable = false)
    private ChatMessageType messageType;

    // Convenience helpers to access detail content (loaded lazily via one-to-one)
    @OneToOne(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private ChatMessageText textDetail;

    @OneToOne(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private ChatMessageFile fileDetail;

    @OneToOne(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private ChatMessageAudio audioDetail;

    @OneToOne(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private ChatMessageVideo videoDetail;
}
