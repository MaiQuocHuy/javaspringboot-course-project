package project.ktc.springboot_app.discount.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.discount.entity.Discount;
import project.ktc.springboot_app.discount.enums.DiscountType;

/** Repository interface for Discount entity operations */
@Repository
public interface DiscountRepository extends JpaRepository<Discount, String> {

  /** Find discount by code (case-insensitive) */
  Optional<Discount> findByCodeIgnoreCase(String code);

  /** Check if discount code exists (case-insensitive) */
  boolean existsByCodeIgnoreCase(String code);

  /** Find all active discounts of a specific type */
  @Query("SELECT d FROM Discount d WHERE d.type = :type AND d.isActive = true")
  List<Discount> findActiveDiscountsByType(@Param("type") DiscountType type);

  /** Find currently valid discounts (active and within date range) */
  @Query(
      "SELECT d FROM Discount d WHERE d.isActive = true "
          + "AND (d.startDate IS NULL OR d.startDate <= :now) "
          + "AND (d.endDate IS NULL OR d.endDate >= :now)")
  List<Discount> findCurrentlyValidDiscounts(@Param("now") LocalDateTime now);

  /** Find discounts by owner user ID */
  @Query("SELECT d FROM Discount d WHERE d.ownerUser.id = :ownerUserId")
  List<Discount> findByOwnerUserId(@Param("ownerUserId") String ownerUserId);

  /** Find discounts by owner user ID with pagination */
  @Query("SELECT d FROM Discount d WHERE d.ownerUser.id = :ownerUserId")
  Page<Discount> findByOwnerUserId(@Param("ownerUserId") String ownerUserId, Pageable pageable);

  /** Find discounts by type with pagination */
  Page<Discount> findByType(DiscountType type, Pageable pageable);

  /** Find active discounts with usage limit not exceeded */
  @Query(
      "SELECT d FROM Discount d WHERE d.isActive = true "
          + "AND d.startDate <= :now AND d.endDate >= :now "
          + "AND (d.usageLimit IS NULL OR SIZE(d.discountUsages) < d.usageLimit)")
  List<Discount> findAvailableDiscounts(@Param("now") LocalDateTime now);

  /** Count total discounts by type */
  long countByType(DiscountType type);

  /** Count active discounts */
  long countByIsActive(boolean isActive);

  /** Find expired discounts that are still active */
  @Query("SELECT d FROM Discount d WHERE d.isActive = true AND d.endDate < :now")
  List<Discount> findExpiredActiveDiscounts(@Param("now") LocalDateTime now);

  /** Find discounts expiring soon (within specified hours) */
  @Query(
      "SELECT d FROM Discount d WHERE d.isActive = true "
          + "AND d.endDate BETWEEN :now AND :expiryThreshold")
  List<Discount> findDiscountsExpiringSoon(
      @Param("now") LocalDateTime now, @Param("expiryThreshold") LocalDateTime expiryThreshold);

  /** Find discount by owner user ID and type */
  @Query("SELECT d FROM Discount d WHERE d.ownerUser.id = :ownerUserId AND d.type = :type")
  Optional<Discount> findByOwnerUserIdAndType(
      @Param("ownerUserId") String ownerUserId, @Param("type") DiscountType type);

  /** Check if user already has a discount of specific type */
  @Query(
      "SELECT COUNT(d) > 0 FROM Discount d WHERE d.ownerUser.id = :ownerUserId AND d.type = :type")
  boolean existsByOwnerUserIdAndType(
      @Param("ownerUserId") String ownerUserId, @Param("type") DiscountType type);

  /** Find active GENERAL discounts that are currently valid (for public use) */
  @Query(
      "SELECT d FROM Discount d WHERE d.type = :type AND d.isActive = :isActive "
          + "AND (d.startDate IS NULL OR d.startDate <= :now) "
          + "AND (d.endDate IS NULL OR d.endDate >= :now)")
  List<Discount> findByTypeAndIsActiveAndCurrentlyValid(
      @Param("type") DiscountType type,
      @Param("isActive") boolean isActive,
      @Param("now") LocalDateTime now);
}
