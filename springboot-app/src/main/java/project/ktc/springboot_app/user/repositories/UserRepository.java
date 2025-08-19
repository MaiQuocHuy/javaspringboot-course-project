package project.ktc.springboot_app.user.repositories;

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
    Optional<User> findByEmail(String email);

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

}
