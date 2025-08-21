package project.ktc.springboot_app.instructor_application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.instructor_application.entity.InstructorApplication;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for InstructorApplication entity
 */
@Repository
public interface InstructorApplicationRepository extends JpaRepository<InstructorApplication, String> {

    /**
     * Find instructor application by user ID
     */
    @Query("SELECT ia FROM InstructorApplication ia WHERE ia.user.id = :userId")
    Optional<InstructorApplication> findByUserId(@Param("userId") String userId);

    /**
     * Check if user has any instructor application
     */
    @Query("SELECT COUNT(ia) > 0 FROM InstructorApplication ia WHERE ia.user.id = :userId")
    boolean existsByUserId(@Param("userId") String userId);

    /**
     * Check if user has pending application
     */
    @Query("SELECT COUNT(ia) > 0 FROM InstructorApplication ia WHERE ia.user.id = :userId AND ia.status = 'PENDING'")
    boolean hasPendingApplication(@Param("userId") String userId);

    /**
     * Check if user has rejected application within recent period
     */
    @Query("SELECT ia FROM InstructorApplication ia WHERE ia.user.id = :userId AND ia.status = 'REJECTED' ORDER BY ia.reviewedAt DESC")
    Optional<InstructorApplication> findLatestRejectedByUserId(@Param("userId") String userId);

    /**
     * Count user's rejected applications
     */
    @Query("SELECT COUNT(ia) FROM InstructorApplication ia WHERE ia.user.id = :userId AND ia.status = 'REJECTED'")
    long countRejectedApplicationsByUserId(@Param("userId") String userId);

    /**
     * Count user's pending applications
     */
    @Query("SELECT COUNT(ia) FROM InstructorApplication ia WHERE ia.user.id = :userId AND ia.status = 'PENDING'")
    long countPendingApplicationsByUserId(@Param("userId") String userId);

    /**
     * Find all instructor applications
     */
    @Query("SELECT ia FROM InstructorApplication ia JOIN FETCH ia.user ORDER BY ia.submittedAt DESC")
    @NonNull
    List<InstructorApplication> findAll();

}
