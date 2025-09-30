package project.ktc.springboot_app.chat.entities;

import jakarta.persistence.*;
import lombok.*;
import project.ktc.springboot_app.entity.BaseEntity;

@Entity
@Table(name = "chat_message_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageType extends BaseEntity {

  @Column(nullable = false, unique = true, length = 50)
  private String name; // TEXT, FILE, AUDIO, VIDEO

  @Column(length = 255)
  private String description;
}
