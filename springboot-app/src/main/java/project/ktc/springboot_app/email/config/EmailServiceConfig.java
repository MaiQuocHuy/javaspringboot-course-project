package project.ktc.springboot_app.email.config;

import com.sendgrid.SendGrid;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * Email service configuration Configures email providers, templates, and async
 * processing
 */
@Configuration
@EnableConfigurationProperties(EmailConfig.class)
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class EmailServiceConfig {

	private final EmailConfig emailConfig;

	/** Configure JavaMailSender for SMTP */
	@Bean
	@ConditionalOnProperty(prefix = "app.email.provider", name = "primary", havingValue = "smtp", matchIfMissing = true)
	public JavaMailSender javaMailSender() {
		log.info("Configuring SMTP email provider");

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		EmailConfig.Smtp smtp = emailConfig.getSmtp();

		mailSender.setHost(smtp.getHost());
		mailSender.setPort(smtp.getPort());
		mailSender.setUsername(smtp.getUsername());
		mailSender.setPassword(smtp.getPassword());

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", smtp.isAuth());
		props.put("mail.smtp.starttls.enable", smtp.isTls());
		props.put("mail.smtp.ssl.enable", smtp.isSsl());
		props.put("mail.smtp.connectiontimeout", smtp.getConnectionTimeout());
		props.put("mail.smtp.timeout", smtp.getReadTimeout());
		props.put("mail.smtp.writetimeout", smtp.getReadTimeout());

		// Additional SMTP properties for better reliability
		props.put("mail.debug", log.isDebugEnabled());
		props.put("mail.smtp.quitwait", false);
		props.put("mail.smtp.socketFactory.fallback", false);

		if (smtp.isSsl()) {
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.port", smtp.getPort());
		}

		log.info(
				"SMTP configuration: host={}, port={}, tls={}, ssl={}",
				smtp.getHost(),
				smtp.getPort(),
				smtp.isTls(),
				smtp.isSsl());

		return mailSender;
	}

	/** Configure SendGrid client */
	@Bean
	@ConditionalOnProperty(prefix = "app.email.provider", name = "primary", havingValue = "sendgrid")
	public SendGrid sendGridClient() {
		log.info("Configuring SendGrid email provider");

		String apiKey = emailConfig.getSendgrid().getApiKey();
		if (apiKey == null || apiKey.trim().isEmpty()) {
			log.warn("SendGrid API key is not configured");
			throw new IllegalArgumentException(
					"SendGrid API key is required when using SendGrid provider");
		}

		return new SendGrid(apiKey);
	}

	/** Configure email template engine */
	@Bean
	public TemplateEngine emailTemplateEngine() {
		log.info("Configuring email template engine");

		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(emailTemplateResolver());

		return templateEngine;
	}

	/** Configure email template resolver */
	@Bean
	public ITemplateResolver emailTemplateResolver() {
		ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		EmailConfig.Template template = emailConfig.getTemplate();

		templateResolver.setPrefix("templates/email/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCharacterEncoding(template.getEncoding());
		templateResolver.setCacheable(template.isCache());
		templateResolver.setCacheTTLMs(template.getCacheDuration() * 1000);
		templateResolver.setOrder(1);

		log.info(
				"Email template resolver configured: cache={}, encoding={}",
				template.isCache(),
				template.getEncoding());

		return templateResolver;
	}

	/** Configure task executor for async email processing */
	@Bean(name = "emailTaskExecutor")
	@Primary
	public TaskExecutor emailTaskExecutor() {
		log.info("Configuring email task executor");

		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("email-");
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(60);

		// Rejection policy
		executor.setRejectedExecutionHandler(
				(r, executor1) -> {
					log.warn("Email task rejected, queue is full. Task: {}", r);
					throw new RuntimeException("Email task queue is full, please try again later");
				});

		executor.initialize();

		log.info("Email task executor configured: corePoolSize=2, maxPoolSize=10, queueCapacity=100");

		return executor;
	}

	/** Log email configuration on startup */
	@Bean
	public EmailConfigurationLogger emailConfigurationLogger() {
		return new EmailConfigurationLogger(emailConfig);
	}

	/** Helper class to log configuration on startup */
	public static class EmailConfigurationLogger {
		private final EmailConfig config;

		public EmailConfigurationLogger(EmailConfig config) {
			this.config = config;
			logConfiguration();
		}

		private void logConfiguration() {
			log.info("=== Email Service Configuration ===");
			log.info("Primary Provider: {}", config.getProvider().getPrimary());
			log.info("Fallback Enabled: {}", config.getProvider().isEnableFallback());
			log.info("Max Retry Attempts: {}", config.getRetry().getMaxAttempts());
			log.info("Template Cache: {}", config.getTemplate().isCache());

			if (config.isSmtpProvider()) {
				log.info("SMTP Host: {}", config.getSmtp().getHost());
				log.info("SMTP Port: {}", config.getSmtp().getPort());
				log.info("SMTP From: {}", config.getSmtp().getFrom());
			}

			if (config.isSendGridProvider()) {
				log.info("SendGrid From: {}", config.getSendgrid().getFrom());
				log.info(
						"SendGrid API Key Configured: {}",
						config.getSendgrid().getApiKey() != null
								&& !config.getSendgrid().getApiKey().trim().isEmpty());
			}

			log.info("=== End Email Configuration ===");
		}
	}
}
