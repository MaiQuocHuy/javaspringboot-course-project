package project.ktc.springboot_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class SpringbootAppApplication {

	public static void main(String[] args) {

		// Environment configuration - check for .env file first, then fall back to
		// system environment variables
		Dotenv tempDotenv = null;
		try {
			tempDotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();
			System.out.println("Loaded .env file successfully");
		} catch (Exception e) {
			System.out.println("No .env file found, using system environment variables");
		}
		final Dotenv dotenv = tempDotenv;

		// Helper method to get environment variable with fallback
		java.util.function.Function<String, String> getEnv = (key) -> {
			if (dotenv != null) {
				String value = dotenv.get(key);
				if (value != null)
					return value;
			}
			return System.getenv(key);
		};

		// Helper method to get environment variable with fallback and default value
		java.util.function.BiFunction<String, String, String> getEnvWithDefault = (key, defaultValue) -> {
			if (dotenv != null) {
				String value = dotenv.get(key, defaultValue);
				if (value != null)
					return value;
			}
			String sysValue = System.getenv(key);
			return sysValue != null ? sysValue : defaultValue;
		};

		// Set system properties from environment variables
		setSystemPropertyIfNotNull("DB_URL", getEnv.apply("DB_URL"));
		setSystemPropertyIfNotNull("DB_USERNAME", getEnv.apply("DB_USERNAME"));
		setSystemPropertyIfNotNull("DB_PASSWORD", getEnv.apply("DB_PASSWORD"));
		setSystemPropertyIfNotNull("JWT_SECRET", getEnv.apply("JWT_SECRET"));
		setSystemPropertyIfNotNull("JWT_EXPIRATION", getEnv.apply("JWT_EXPIRATION"));
		setSystemPropertyIfNotNull("REFRESH_EXPIRATION", getEnv.apply("REFRESH_EXPIRATION"));
		setSystemPropertyIfNotNull("CLOUDINARY_CLOUD_NAME", getEnv.apply("CLOUDINARY_CLOUD_NAME"));
		setSystemPropertyIfNotNull("CLOUDINARY_API_KEY", getEnv.apply("CLOUDINARY_API_KEY"));
		setSystemPropertyIfNotNull("CLOUDINARY_API_SECRET", getEnv.apply("CLOUDINARY_API_SECRET"));
		setSystemPropertyIfNotNull("STRIPE_SECRET_KEY", getEnv.apply("STRIPE_SECRET_KEY"));
		setSystemPropertyIfNotNull("STRIPE_PUBLISHABLE_KEY", getEnv.apply("STRIPE_PUBLISHABLE_KEY"));
		setSystemPropertyIfNotNull("STRIPE_WEBHOOK_SECRET", getEnvWithDefault.apply("STRIPE_WEBHOOK_SECRET", ""));
		setSystemPropertyIfNotNull("FRONTEND_URL", getEnvWithDefault.apply("FRONTEND_URL", ""));
		setSystemPropertyIfNotNull("EMAIL_SMTP_HOST", getEnv.apply("EMAIL_SMTP_HOST"));
		setSystemPropertyIfNotNull("EMAIL_SMTP_PORT", getEnv.apply("EMAIL_SMTP_PORT"));
		setSystemPropertyIfNotNull("EMAIL_SMTP_USERNAME", getEnv.apply("EMAIL_SMTP_USERNAME"));
		setSystemPropertyIfNotNull("EMAIL_SMTP_PASSWORD", getEnv.apply("EMAIL_SMTP_PASSWORD"));
		setSystemPropertyIfNotNull("EMAIL_FROM", getEnv.apply("EMAIL_FROM"));
		setSystemPropertyIfNotNull("EMAIL_FROM_NAME", getEnv.apply("EMAIL_FROM_NAME"));
		setSystemPropertyIfNotNull("REDIS_HOST", getEnvWithDefault.apply("REDIS_HOST", ""));
		setSystemPropertyIfNotNull("REDIS_PORT", getEnvWithDefault.apply("REDIS_PORT", ""));
		setSystemPropertyIfNotNull("REDIS_PASSWORD", getEnv.apply("REDIS_PASSWORD"));
		setSystemPropertyIfNotNull("REDIS_SSL_ENABLED", getEnvWithDefault.apply("REDIS_SSL_ENABLED", "true"));
		setSystemPropertyIfNotNull("UPSTASH_REDIS_REST_URL", getEnv.apply("UPSTASH_REDIS_REST_URL"));
		setSystemPropertyIfNotNull("UPSTASH_REDIS_REST_TOKEN", getEnv.apply("UPSTASH_REDIS_REST_TOKEN"));

		SpringApplication.run(SpringbootAppApplication.class, args);
	}

	private static void setSystemPropertyIfNotNull(String key, String value) {
		if (value != null && !value.trim().isEmpty()) {
			System.setProperty(key, value);
		}
	}
}
