package project.ktc.springboot_app.email.interfaces;

import java.util.Map;

/** Template service interface for processing email templates */
public interface EmailTemplateService {

	/**
	 * Process email template with variables
	 *
	 * @param templateName
	 *            template name (without .html extension)
	 * @param variables
	 *            template variables
	 * @return processed template content
	 */
	String processTemplate(String templateName, Map<String, Object> variables);

	/**
	 * Check if template exists
	 *
	 * @param templateName
	 *            template name
	 * @return true if template exists
	 */
	boolean templateExists(String templateName);

	/**
	 * Get default template variables
	 *
	 * @return default variables available in all templates
	 */
	Map<String, Object> getDefaultVariables();

	/**
	 * Process inline template content
	 *
	 * @param templateContent
	 *            template content as string
	 * @param variables
	 *            template variables
	 * @return processed content
	 */
	String processInlineTemplate(String templateContent, Map<String, Object> variables);
}
