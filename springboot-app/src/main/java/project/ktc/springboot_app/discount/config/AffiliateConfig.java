package project.ktc.springboot_app.discount.config;

import java.math.BigDecimal;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Configuration properties for affiliate commission system */
@Component
@ConfigurationProperties(prefix = "app.affiliate")
@Data
public class AffiliateConfig {

	/** Commission configuration */
	private Commission commission = new Commission();

	@Data
	public static class Commission {
		/**
		 * Default commission percentage for referral payouts This is applied to the
		 * final price (after
		 * discount)
		 */
		private BigDecimal percent = new BigDecimal("3.0");

		/** Whether affiliate commission system is enabled */
		private boolean enabled = true;
	}

	/** Get commission percentage as decimal (e.g., 3.0 for 3%) */
	public BigDecimal getCommissionPercent() {
		return commission.percent;
	}

	/** Check if affiliate commission is enabled */
	public boolean isCommissionEnabled() {
		return commission.enabled;
	}
}
