package project.ktc.springboot_app.permission.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.permission.entity.FilterType;

import java.util.Optional;

/**
 * Repository for FilterType operations
 */
@Repository
public interface FilterTypeRepository extends JpaRepository<FilterType, String> {

    /**
     * Find filter type by name
     * 
     * @param name the filter type name
     * @return optional filter type
     */
    Optional<FilterType> findByName(String name);

    /**
     * Check if filter type name exists
     * 
     * @param name the filter type name
     * @return true if exists
     */
    boolean existsByName(String name);

    /**
     * Get the "ALL" filter type
     * 
     * @return the ALL filter type
     */
    @Query("SELECT ft FROM FilterType ft WHERE ft.id = 'filter-type-001'")
    Optional<FilterType> getAllFilterType();

    /**
     * Get the "OWN" filter type
     * 
     * @return the OWN filter type
     */
    @Query("SELECT ft FROM FilterType ft WHERE ft.id = 'filter-type-002'")
    Optional<FilterType> getOwnFilterType();
}
