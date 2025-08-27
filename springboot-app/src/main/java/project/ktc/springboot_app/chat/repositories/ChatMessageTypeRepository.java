package project.ktc.springboot_app.chat.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import project.ktc.springboot_app.chat.entities.ChatMessageType;

import java.util.Optional;

public interface ChatMessageTypeRepository extends JpaRepository<ChatMessageType, String> {
    Optional<ChatMessageType> findByNameIgnoreCase(String name);
}
