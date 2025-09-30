package project.ktc.springboot_app.utils;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/** Utility class for extracting payment details from Stripe Session ID */
@Slf4j
@UtilityClass
public class ExtractPaymentDetailFromSessionId {

  /**
   * Extracts payment metadata from Stripe session ID
   *
   * @param sessionId The Stripe session ID
   * @return PaymentDetailDto containing all metadata information
   * @throws StripeException if there's an error retrieving the session
   */
  public PaymentDetailDto extractPaymentDetails(String sessionId) throws StripeException {
    try {
      log.info("Extracting payment details from session ID: {}", sessionId);

      // Retrieve session from Stripe
      Session session = Session.retrieve(sessionId);

      if (session == null) {
        log.error("Session not found for ID: {}", sessionId);
        throw new IllegalArgumentException("Session not found with ID: " + sessionId);
      }

      Map<String, String> metadata = session.getMetadata();

      if (metadata == null || metadata.isEmpty()) {
        log.warn("No metadata found for session ID: {}", sessionId);
        return PaymentDetailDto.builder().build();
      }

      // Extract metadata values
      String userId = metadata.get("userId");
      String courseId = metadata.get("courseId");
      String paymentId = metadata.get("paymentId");
      String originalPriceStr = metadata.get("originalPrice");
      String finalPriceStr = metadata.get("finalPrice");
      String discountAmountStr = metadata.get("discountAmount");
      String discountCode = metadata.get("discountCode");

      // Convert string prices to BigDecimal
      BigDecimal originalPrice = parsePrice(originalPriceStr);
      BigDecimal finalPrice = parsePrice(finalPriceStr);
      BigDecimal discountAmount = parsePrice(discountAmountStr);

      PaymentDetailDto result =
          PaymentDetailDto.builder()
              .sessionId(sessionId)
              .userId(userId)
              .courseId(courseId)
              .paymentId(paymentId)
              .originalPrice(originalPrice)
              .finalPrice(finalPrice)
              .discountAmount(discountAmount)
              .discountCode(discountCode)
              .discountApplied(
                  discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0)
              .paymentStatus(session.getPaymentStatus())
              .sessionStatus(session.getStatus())
              .customerEmail(session.getCustomerEmail())
              .totalAmount(
                  session.getAmountTotal() != null
                      ? BigDecimal.valueOf(session.getAmountTotal()).divide(BigDecimal.valueOf(100))
                      : null)
              .currency(session.getCurrency())
              .build();

      log.info("Successfully extracted payment details for session: {}", sessionId);
      return result;

    } catch (StripeException e) {
      log.error(
          "Failed to extract payment details from session ID {}: {}", sessionId, e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error(
          "Unexpected error extracting payment details from session ID {}: {}",
          sessionId,
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to extract payment details", e);
    }
  }

  /**
   * Retrieves only metadata from Stripe session
   *
   * @param sessionId The Stripe session ID
   * @return Map containing all metadata key-value pairs
   * @throws StripeException if there's an error retrieving the session
   */
  public Map<String, String> extractMetadata(String sessionId) throws StripeException {
    try {
      log.info("Extracting metadata from session ID: {}", sessionId);

      Session session = Session.retrieve(sessionId);

      if (session == null) {
        log.error("Session not found for ID: {}", sessionId);
        throw new IllegalArgumentException("Session not found with ID: " + sessionId);
      }

      Map<String, String> metadata = session.getMetadata();
      log.info(
          "Successfully extracted metadata for session: {} with {} entries",
          sessionId,
          metadata != null ? metadata.size() : 0);

      return metadata;

    } catch (StripeException e) {
      log.error("Failed to extract metadata from session ID {}: {}", sessionId, e.getMessage(), e);
      throw e;
    }
  }

  /** Safely parses a price string to BigDecimal */
  private BigDecimal parsePrice(String priceStr) {
    if (priceStr == null || priceStr.trim().isEmpty()) {
      return null;
    }

    try {
      return new BigDecimal(priceStr);
    } catch (NumberFormatException e) {
      log.warn("Failed to parse price string: {}", priceStr);
      return null;
    }
  }

  /** DTO class for payment details extracted from Stripe session */
  @Data
  @Builder
  public static class PaymentDetailDto {
    private String sessionId;
    private String userId;
    private String courseId;
    private String paymentId;
    private BigDecimal originalPrice;
    private BigDecimal finalPrice;
    private BigDecimal discountAmount;
    private String discountCode;
    private Boolean discountApplied;

    // Additional Stripe session information
    private String paymentStatus;
    private String sessionStatus;
    private String customerEmail;
    private BigDecimal totalAmount;
    private String currency;
  }
}
