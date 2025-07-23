package project.ktc.springboot_app.video.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.entity.VideoContent;

import java.util.Optional;

@Repository
public interface VideoContentRepository extends JpaRepository<VideoContent, String> {

    /**
     * Find VideoContent by uploader
     */
    Optional<VideoContent> findByUploadedById(String uploadedById);
}
