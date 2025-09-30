package project.ktc.springboot_app.stripe.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe configuration class that initializes the Stripe API key and provides
 * configuration for
 * webhook handling.
 */
@Configuration
@Slf4j
public class StripeConfig {

	@Value("${stripe.secretKey}")
	private String secretKey;

	@Value("${stripe.publishableKey}")
	private String publishableKey;

	@Value("${stripe.webhook.secret:}")
	private String webhookSecret;

	@PostConstruct
	public void init() {
		// Initialize Stripe with secret key
		Stripe.apiKey = secretKey;
		log.info("Stripe API initialized with secret key");

		if (webhookSecret != null && !webhookSecret.isEmpty()) {
			log.info("Stripe webhook endpoint secret configured");
		} else {
			log.warn(
					"Stripe webhook endpoint secret not configured - webhook signature verification will be skipped in development");
		}
	}

	public String getWebhookSecret() {
		return webhookSecret;
	}

	public String getPublishableKey() {
		return publishableKey;
	}
}
