package project.ktc.springboot_app.email.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/** Email configuration properties Binds application.yml email configuration to Java object */
@Configuration
@ConfigurationProperties(prefix = "app.email")
@Data
@Validated
@Slf4j
public class EmailConfig {

  /** Email provider configuration */
  private Provider provider = new Provider();

  /** SMTP configuration */
  private Smtp smtp = new Smtp();

  /** SendGrid configuration */
  private Sendgrid sendgrid = new Sendgrid();

  /** Retry configuration */
  private Retry retry = new Retry();

  /** Template configuration */
  private Template template = new Template();

  @Data
  public static class Provider {
    /** Primary email provider (smtp or sendgrid) */
    @NotBlank private String primary = "smtp";

    /** Fallback email provider */
    private String fallback = "smtp";

    /** Enable provider fallback on failure */
    private boolean enableFallback = true;
  }

  @Data
  public static class Smtp {
    /** SMTP host */
    @NotBlank private String host = "localhost";

    /** SMTP port */
    @Positive private int port = 587;

    /** SMTP username */
    private String username;

    /** SMTP password */
    private String password;

    /** Enable TLS */
    private boolean tls = true;

    /** Enable SSL */
    private boolean ssl = false;

    /** Enable authentication */
    private boolean auth = true;

    /** Default from email address */
    @NotBlank private String from = "noreply@example.com";

    /** Default from name */
    private String fromName = "KTC Learning Platform";

    /** Connection timeout in milliseconds */
    private int connectionTimeout = 10000;

    /** Read timeout in milliseconds */
    private int readTimeout = 10000;
  }

  @Data
  public static class Sendgrid {
    /** SendGrid API key */
    private String apiKey;

    /** Default from email address */
    @NotBlank private String from = "noreply@example.com";

    /** Default from name */
    private String fromName = "KTC Learning Platform";

    /** SendGrid template ID for default template */
    private String defaultTemplateId;

    /** Enable click tracking */
    private boolean clickTracking = true;

    /** Enable open tracking */
    private boolean openTracking = true;
  }

  @Data
  public static class Retry {
    /** Maximum retry attempts */
    @Positive private int maxAttempts = 3;

    /** Initial delay in seconds */
    @Positive private long initialDelay = 60;

    /** Maximum delay in seconds */
    @Positive private long maxDelay = 3600;

    /** Backoff multiplier */
    private double backoffMultiplier = 2.0;

    /** Enable exponential backoff */
    private boolean exponentialBackoff = true;
  }

  @Data
  public static class Template {
    /** Template base path */
    private String basePath = "classpath:templates/email";

    /** Default template encoding */
    private String encoding = "UTF-8";

    /** Cache templates */
    private boolean cache = true;

    /** Template cache duration in seconds */
    private long cacheDuration = 3600;
  }

  /** Get the active email provider type */
  public String getActiveProvider() {
    return provider.getPrimary();
  }

  /** Check if provider is SMTP */
  public boolean isSmtpProvider() {
    return "smtp".equalsIgnoreCase(provider.getPrimary());
  }

  /** Check if provider is SendGrid */
  public boolean isSendGridProvider() {
    return "sendgrid".equalsIgnoreCase(provider.getPrimary());
  }

  /** Get fallback provider if enabled */
  public String getFallbackProvider() {
    return provider.isEnableFallback() ? provider.getFallback() : null;
  }
}
