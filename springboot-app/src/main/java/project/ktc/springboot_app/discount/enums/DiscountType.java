package project.ktc.springboot_app.discount.enums;

/** Enum representing different types of discounts */
public enum DiscountType {
	/**
	 * General discount available to all users owner_user_id should be null for this
	 * type
	 */
	GENERAL,

	/**
	 * Referral discount created by specific users owner_user_id is required for
	 * this type
	 */
	REFERRAL
}
