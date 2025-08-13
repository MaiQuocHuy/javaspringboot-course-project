package project.ktc.springboot_app.email.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import project.ktc.springboot_app.email.config.EmailConfig;
import project.ktc.springboot_app.email.interfaces.EmailTemplateService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Email template service implementation using Thymeleaf
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateServiceImp implements EmailTemplateService {

    private final TemplateEngine templateEngine;
    private final EmailConfig emailConfig;

    @Override
    public String processTemplate(String templateName, Map<String, Object> variables) {
        log.debug("Processing template: {} with {} variables", templateName, variables.size());

        try {
            Context context = new Context(Locale.getDefault());

            // Add default variables
            Map<String, Object> allVariables = new HashMap<>(getDefaultVariables());
            if (variables != null) {
                allVariables.putAll(variables);
            }

            context.setVariables(allVariables);

            String result = templateEngine.process(templateName, context);

            log.debug("Template {} processed successfully", templateName);
            return result;

        } catch (Exception e) {
            log.error("Error processing template: {}", templateName, e);
            throw new RuntimeException("Failed to process email template: " + templateName, e);
        }
    }

    @Override
    public boolean templateExists(String templateName) {
        try {
            // Try to process template with empty context to check if it exists
            Context context = new Context();
            templateEngine.process(templateName, context);
            return true;
        } catch (Exception e) {
            log.debug("Template {} does not exist or is invalid", templateName);
            return false;
        }
    }

    @Override
    public Map<String, Object> getDefaultVariables() {
        Map<String, Object> defaultVars = new HashMap<>();

        // Current date and time
        LocalDateTime now = LocalDateTime.now();
        defaultVars.put("currentDate", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        defaultVars.put("currentDateTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        defaultVars.put("currentYear", String.valueOf(now.getYear()));

        // Application information
        defaultVars.put("appName", "KTC Learning Platform");
        defaultVars.put("appUrl", "https://ktc-learning.com"); // This should come from configuration
        defaultVars.put("supportEmail", emailConfig.getSmtp().getFrom());

        // Common email styling variables
        defaultVars.put("primaryColor", "#007bff");
        defaultVars.put("secondaryColor", "#6c757d");
        defaultVars.put("successColor", "#28a745");
        defaultVars.put("warningColor", "#ffc107");
        defaultVars.put("dangerColor", "#dc3545");

        return defaultVars;
    }

    @Override
    public String processInlineTemplate(String templateContent, Map<String, Object> variables) {
        log.debug("Processing inline template with {} variables", variables != null ? variables.size() : 0);

        try {
            Context context = new Context(Locale.getDefault());

            // Add default variables
            Map<String, Object> allVariables = new HashMap<>(getDefaultVariables());
            if (variables != null) {
                allVariables.putAll(variables);
            }

            context.setVariables(allVariables);

            // Create a simple template resolver for inline content
            String result = templateEngine.process("inline:" + templateContent, context);

            log.debug("Inline template processed successfully");
            return result;

        } catch (Exception e) {
            log.error("Error processing inline template", e);
            throw new RuntimeException("Failed to process inline email template", e);
        }
    }
}
