package project.ktc.springboot_app.discount.enums;

/** Enum representing the status of affiliate payouts */
public enum PayoutStatus {
  /** Payout is pending processing */
  PENDING,

  /** Payout has been successfully paid */
  PAID,

  /** Payout has been cancelled */
  CANCELLED
}
