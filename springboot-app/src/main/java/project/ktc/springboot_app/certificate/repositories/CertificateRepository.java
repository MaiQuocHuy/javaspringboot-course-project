package project.ktc.springboot_app.certificate.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.certificate.entity.Certificate;

import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, String> {

       /**
        * Check if a certificate already exists for a specific user and course
        */
       @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
                     "FROM Certificate c WHERE c.user.id = :userId AND c.course.id = :courseId")
       boolean existsByUserIdAndCourseId(@Param("userId") String userId, @Param("courseId") String courseId);

       /**
        * Find certificate by user ID and course ID
        */
       @Query("SELECT c FROM Certificate c " +
                     "LEFT JOIN FETCH c.user u " +
                     "LEFT JOIN FETCH c.course co " +
                     "LEFT JOIN FETCH co.instructor i " +
                     "WHERE c.user.id = :userId AND c.course.id = :courseId")
       Optional<Certificate> findByUserIdAndCourseId(@Param("userId") String userId,
                     @Param("courseId") String courseId);

       /**
        * Find certificate by certificate code
        */
       @Query("SELECT c FROM Certificate c " +
                     "LEFT JOIN FETCH c.user u " +
                     "LEFT JOIN FETCH c.course co " +
                     "LEFT JOIN FETCH co.instructor i " +
                     "WHERE c.certificateCode = :certificateCode")
       Optional<Certificate> findByCertificateCode(@Param("certificateCode") String certificateCode);

       /**
        * Find all certificates for a specific user
        */
       @Query("SELECT c FROM Certificate c " +
                     "LEFT JOIN FETCH c.course co " +
                     "LEFT JOIN FETCH co.instructor i " +
                     "WHERE c.user.id = :userId " +
                     "ORDER BY c.issuedAt DESC")
       Page<Certificate> findByUserId(@Param("userId") String userId, Pageable pageable);

       /**
        * Find all certificates for a specific course
        */
       @Query("SELECT c FROM Certificate c " +
                     "LEFT JOIN FETCH c.user u " +
                     "LEFT JOIN FETCH c.course co " +
                     "WHERE c.course.id = :courseId " +
                     "ORDER BY c.issuedAt DESC")
       Page<Certificate> findByCourseId(@Param("courseId") String courseId, Pageable pageable);

       /**
        * Find all certificates with pagination and optional search
        */
       @Query("SELECT c FROM Certificate c " +
                     "LEFT JOIN FETCH c.user u " +
                     "LEFT JOIN FETCH c.course co " +
                     "LEFT JOIN FETCH co.instructor i " +
                     "WHERE (:search IS NULL OR " +
                     "       LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "       LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "       LOWER(co.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "       LOWER(c.certificateCode) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                     "ORDER BY c.issuedAt DESC")
       Page<Certificate> findAllWithSearch(@Param("search") String search, Pageable pageable);

       /**
        * Count total certificates
        */
       @Query("SELECT COUNT(c) FROM Certificate c")
       Long countTotalCertificates();

       /**
        * Count certificates for a specific user
        */
       @Query("SELECT COUNT(c) FROM Certificate c WHERE c.user.id = :userId")
       Long countByUserId(@Param("userId") String userId);

       /**
        * Count certificates for a specific course
        */
       @Query("SELECT COUNT(c) FROM Certificate c WHERE c.course.id = :courseId")
       Long countByCourseId(@Param("courseId") String courseId);

       /**
        * Check if certificate code exists (for uniqueness validation)
        */
       boolean existsByCertificateCode(String certificateCode);
}
