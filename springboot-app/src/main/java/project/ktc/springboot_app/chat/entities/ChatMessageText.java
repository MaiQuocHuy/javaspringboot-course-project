package project.ktc.springboot_app.chat.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_message_texts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageText {
	@Id
	private String id; // same as parent message id (UUID String)

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "id")
	private ChatMessage message;

	@Lob
	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;
}
