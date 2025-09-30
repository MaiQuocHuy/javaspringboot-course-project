package project.ktc.springboot_app.upload.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Cloudinary configuration for image upload service */
@Configuration
@Slf4j
public class CloudinaryConfig {

	@Value("${cloudinary.cloudName}")
	private String cloudName;

	@Value("${cloudinary.apiKey}")
	private String apiKey;

	@Value("${cloudinary.apiSecret}")
	private String apiSecret;

	/** Configure Cloudinary client bean */
	@Bean
	public Cloudinary cloudinary() {
		log.info("Initializing Cloudinary with cloud name: {}", cloudName);

		return new Cloudinary(
				ObjectUtils.asMap(
						"cloud_name", cloudName,
						"api_key", apiKey,
						"api_secret", apiSecret,
						"secure", true // Always use HTTPS
				));
	}
}
