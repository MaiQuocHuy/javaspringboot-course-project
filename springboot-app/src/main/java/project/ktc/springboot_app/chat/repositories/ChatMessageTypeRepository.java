package project.ktc.springboot_app.chat.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import project.ktc.springboot_app.chat.entities.ChatMessageType;

public interface ChatMessageTypeRepository extends JpaRepository<ChatMessageType, String> {
  Optional<ChatMessageType> findByNameIgnoreCase(String name);
}
