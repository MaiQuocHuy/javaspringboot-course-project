package project.ktc.springboot_app.review.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.review.entity.Review;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

        /**
         * Check if a user has already reviewed a specific course
         */
        @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.user.id = :userId AND r.course.id = :courseId")
        boolean existsByUserIdAndCourseId(@Param("userId") String userId, @Param("courseId") String courseId);

        /**
         * Find a review by user and course
         */
        @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.course.id = :courseId")
        Optional<Review> findByUserIdAndCourseId(@Param("userId") String userId, @Param("courseId") String courseId);

        /**
         * Find all reviews for a specific course with pagination
         */
        @Query("SELECT r FROM Review r " +
                        "JOIN FETCH r.user u " +
                        "WHERE r.course.id = :courseId " +
                        "ORDER BY r.reviewedAt DESC")
        Page<Review> findByCourseIdWithUser(@Param("courseId") String courseId,
                        Pageable pageable);

        /**
         * Find all reviews by a specific user
         */
        @Query("SELECT r FROM Review r " +
                        "JOIN FETCH r.course c " +
                        "WHERE r.user.id = :userId " +
                        "ORDER BY r.reviewedAt DESC")
        List<Review> findByUserIdWithCourse(@Param("userId") String userId);

        /**
         * Find all reviews by a specific user with pagination and sorting
         */
        @Query("SELECT r FROM Review r " +
                        "JOIN FETCH r.course c " +
                        "WHERE r.user.id = :userId")
        Page<Review> findByUserIdWithCourse(@Param("userId") String userId, Pageable pageable);

        /**
         * Get average rating for a course
         */
        @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
        Double getAverageRatingByCourseId(@Param("courseId") String courseId);

        /**
         * Count total reviews for a course
         */
        @Query("SELECT COUNT(r) FROM Review r WHERE r.course.id = :courseId")
        Long countByCourseId(@Param("courseId") String courseId);

        /**
         * Get rating distribution for a course
         */
        @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.course.id = :courseId GROUP BY r.rating ORDER BY r.rating DESC")
        List<Object[]> getRatingDistributionByCourseId(@Param("courseId") String courseId);

        /**
         * Get course slug
         */
        @Query("SELECT r FROM Review r " +
                        "JOIN FETCH r.user u " +
                        "WHERE r.course.slug = :courseSlug " +
                        "ORDER BY r.reviewedAt DESC")
        Page<Review> findByCourseSlugWithUser(@Param("courseSlug") String courseSlug, Pageable pageable);
}
