package project.ktc.springboot_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class SpringbootAppApplication {

	public static void main(String[] args) {

		// Dotenv configuration
		Dotenv dotenv = Dotenv.load();

		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
		System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));
		System.setProperty("REFRESH_EXPIRATION", dotenv.get("REFRESH_EXPIRATION"));
		System.setProperty("CLOUDINARY_CLOUD_NAME", dotenv.get("CLOUDINARY_CLOUD_NAME"));
		System.setProperty("CLOUDINARY_API_KEY", dotenv.get("CLOUDINARY_API_KEY"));
		System.setProperty("CLOUDINARY_API_SECRET", dotenv.get("CLOUDINARY_API_SECRET"));
		System.setProperty("STRIPE_SECRET_KEY", dotenv.get("STRIPE_SECRET_KEY"));
		System.setProperty("STRIPE_PUBLISHABLE_KEY", dotenv.get("STRIPE_PUBLISHABLE_KEY"));
		System.setProperty("STRIPE_WEBHOOK_SECRET", dotenv.get("STRIPE_WEBHOOK_SECRET", ""));
		System.setProperty("FRONTEND_URL", dotenv.get("FRONTEND_URL", "http://localhost:3000"));
		SpringApplication.run(SpringbootAppApplication.class, args);
	}
}
