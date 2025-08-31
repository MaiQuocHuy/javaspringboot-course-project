package project.ktc.springboot_app.user.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.auth.entitiy.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
        @SuppressWarnings("null")
        Optional<User> findById(String id);

        Optional<User> findByEmail(String email);

        Optional<User> findByName(String name);

        @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.email = :email")
        Optional<User> findByEmailWithRoles(@Param("email") String email);

        @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE " +
                        "(:search IS NULL OR :search = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
                        +
                        "(:role IS NULL OR :role = '' OR u.role.role = :role) AND " +
                        "(:isActive IS NULL OR u.isActive = :isActive)")
        Page<User> findUsersWithFilters(@Param("search") String search,
                        @Param("role") String role,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);

        // Get enrolled courses with payment info and study time for a user
        @Query("SELECT " +
                        "e.course.id, " +
                        "e.course.title, " +
                        "e.course.instructor.name, " +
                        "e.enrolledAt, " +
                        "e.completionStatus, " +
                        "COALESCE(p.amount, 0), " +
                        "COALESCE((" +
                        "   SELECT SUM(l.content.duration) " +
                        "   FROM LessonCompletion lc " +
                        "   JOIN lc.lesson l " +
                        "   JOIN l.content vc " +
                        "   JOIN l.section s " +
                        "   WHERE lc.user.id = e.user.id " +
                        "   AND s.course.id = e.course.id " +
                        "   AND vc.duration IS NOT NULL" +
                        "), 0) " +
                        "FROM Enrollment e " +
                        "LEFT JOIN e.course c " +
                        "LEFT JOIN Payment p ON p.user.id = e.user.id AND p.course.id = e.course.id AND p.status = 'COMPLETED' "
                        +
                        "WHERE e.user.id = :userId " +
                        "ORDER BY e.enrolledAt DESC")
        List<Object[]> findEnrolledCoursesWithPaymentByUserId(@Param("userId") String userId);

        // Get total payments amount for a user
        @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.user.id = :userId AND p.status = 'COMPLETED'")
        BigDecimal getTotalPaymentsByUserId(@Param("userId") String userId);

        // Get total study time from completed lessons for a user
        @Query("SELECT COALESCE(SUM(l.content.duration), 0) " +
                        "FROM LessonCompletion lc " +
                        "JOIN lc.lesson l " +
                        "JOIN l.content vc " +
                        "WHERE lc.user.id = :userId AND vc.duration IS NOT NULL")
        Long getTotalStudyTimeByUserId(@Param("userId") String userId);

}
