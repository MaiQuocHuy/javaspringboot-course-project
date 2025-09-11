package project.ktc.springboot_app.instructor_application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.instructor_application.entity.InstructorApplication;
import project.ktc.springboot_app.instructor_application.entity.InstructorApplication.ApplicationStatus;

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
    @Query("SELECT ia FROM InstructorApplication ia WHERE ia.user.id = :userId AND ia.isDeleted = false ORDER BY ia.submittedAt DESC")
    Optional<InstructorApplication> findByUserId(@Param("userId") String userId);

    /**
     * Find instructor application by user ID
     */
    @Query("SELECT ia FROM InstructorApplication ia WHERE ia.user.id = :userId AND ia.isDeleted = false ORDER BY ia.submittedAt DESC")
    List<InstructorApplication> findByUserIdAdmin(@Param("userId") String userId);

    /**
     * Check if user has any instructor application
     */
    @Query("SELECT COUNT(ia) > 0 FROM InstructorApplication ia WHERE ia.user.id = :userId AND ia.isDeleted = false")
    boolean existsByUserId(@Param("userId") String userId);

    /**
     * Check if user already has an application in pending or approved status
     */
    @Query("SELECT ia.status FROM InstructorApplication ia WHERE ia.user.id = :userId AND ia.status IN ('PENDING', 'APPROVED') AND ia.isDeleted = false")
    List<InstructorApplication.ApplicationStatus> findActiveStatusesByUserId(@Param("userId") String userId);

    /**
     * Check if user has rejected application within recent period
     */
    @Query("SELECT ia FROM InstructorApplication ia WHERE ia.user.id = :userId AND ia.status = 'REJECTED' AND ia.isDeleted = false ORDER BY ia.reviewedAt DESC")
    Optional<InstructorApplication> findLatestRejectedByUserId(@Param("userId") String userId);

    /**
     * Count user's rejected applications
     */
    @Query("SELECT COUNT(ia) FROM InstructorApplication ia WHERE ia.user.id = :userId AND ia.status = 'REJECTED' AND ia.isDeleted = false")
    long countRejectedApplicationsByUserId(@Param("userId") String userId);

    /**
     * Count user's pending applications
     */
    @Query("SELECT COUNT(ia) FROM InstructorApplication ia WHERE ia.user.id = :userId AND ia.status = 'PENDING' AND ia.isDeleted = false")
    long countPendingApplicationsByUserId(@Param("userId") String userId);

    /**
     * Find all instructor applications
     */
    @Query("SELECT ia FROM InstructorApplication ia JOIN FETCH ia.user WHERE ia.isDeleted = false ORDER BY ia.submittedAt DESC")
    @NonNull
    List<InstructorApplication> findAll();

    /**
     * Find all instructor applications (1 latest per user)
     */
    @Query("""
            SELECT ia
            FROM InstructorApplication ia
            JOIN FETCH ia.user u
            WHERE ia.isDeleted = false AND ia.submittedAt = (
                SELECT MAX(ia2.submittedAt)
                FROM InstructorApplication ia2
                WHERE ia2.user.id = u.id AND ia2.isDeleted = false
            )
            ORDER BY ia.submittedAt DESC
            """)
    @NonNull
    List<InstructorApplication> findAllLatestPerUser();

    /**
     * Find latest instructor application by user ID
     */
    @Query("SELECT ia FROM InstructorApplication ia WHERE ia.user.id = :userId AND ia.isDeleted = false ORDER BY ia.submittedAt DESC LIMIT 1")
    Optional<InstructorApplication> findFirstByUserIdOrderBySubmittedAtDesc(@Param("userId") String userId);

    /**
     * Find latest instructor application status by user ID
     */
    @Query("SELECT ia.status FROM InstructorApplication ia WHERE ia.user.id = :userId AND ia.isDeleted = false ORDER BY ia.submittedAt DESC LIMIT 1")
    Optional<ApplicationStatus> findLatestApplicationStatusByUserId(@Param("userId") String userId);

    /**
     * Find application by ID excluding soft deleted records
     */
    @Query("SELECT ia FROM InstructorApplication ia WHERE ia.id = :id AND ia.isDeleted = false")
    Optional<InstructorApplication> findByIdAndNotDeleted(@Param("id") String id);
}
