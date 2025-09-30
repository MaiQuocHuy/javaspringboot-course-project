package project.ktc.springboot_app.chat.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_message_videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageVideo {
  @Id private String id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "id")
  private ChatMessage message;

  @Column(name = "file_url", nullable = false, length = 1000)
  private String videoUrl;

  @Column(name = "file_name", length = 255)
  private String fileName;

  @Column(name = "mime_type", length = 150)
  private String mimeType;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "duration")
  private Long duration; // Duration in seconds

  @Column(name = "resolution", length = 50)
  private String resolution; // e.g. 1920x1080

  @Column(name = "thumbnail_url", length = 1000)
  private String thumbnailUrl;
}
