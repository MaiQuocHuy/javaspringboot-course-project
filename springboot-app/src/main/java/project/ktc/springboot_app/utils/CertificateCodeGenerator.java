package project.ktc.springboot_app.utils;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for generating unique certificate codes
 *
 * <p>
 * Certificate Code Format: [COURSE_PREFIX]-[YEAR]-[SEQUENCE]-[RANDOM_ID]
 * Example:
 * RC-2025-001-ABCD1234
 */
@UtilityClass
@Slf4j
public class CertificateCodeGenerator {

	private static final int MAX_PREFIX_LENGTH = 4;
	private static final int SEQUENCE_DIGITS = 3;
	private static final int RANDOM_ID_LENGTH = 8;

	/**
	 * Generate a unique certificate code based on course title and current
	 * timestamp
	 *
	 * @param courseTitle
	 *            The title of the course
	 * @param sequence
	 *            Sequential number for uniqueness
	 * @return Formatted certificate code
	 */
	public static String generateCertificateCode(String courseTitle, int sequence) {
		try {
			String coursePrefix = generateCoursePrefix(courseTitle);
			String year = String.valueOf(LocalDateTime.now().getYear());
			String sequenceStr = String.format("%0" + SEQUENCE_DIGITS + "d", sequence);
			String randomId = generateRandomId();

			return String.format("%s-%s-%s-%s", coursePrefix, year, sequenceStr, randomId);
		} catch (Exception e) {
			log.error("Error generating certificate code for course: {}", courseTitle, e);
			// Fallback to generic code
			return generateFallbackCode(sequence);
		}
	}

	/**
	 * Generate course prefix from course title Extracts meaningful characters from
	 * the course title
	 * to create a readable prefix
	 *
	 * @param courseTitle
	 *            The title of the course
	 * @return Course prefix (2-4 characters)
	 */
	public static String generateCoursePrefix(String courseTitle) {
		if (courseTitle == null || courseTitle.trim().isEmpty()) {
			return "GEN"; // Generic prefix for empty/null titles
		}

		String title = courseTitle.trim().toUpperCase();
		StringBuilder prefix = new StringBuilder();

		// First, try to extract first letter of each meaningful word
		String[] words = title.split("\\s+");
		for (String word : words) {
			if (word.length() > 0 && prefix.length() < MAX_PREFIX_LENGTH) {
				// Skip common words like "THE", "AND", "OF", etc.
				if (!isCommonWord(word)) {
					prefix.append(word.charAt(0));
				}
			}
		}

		// If we don't have enough characters, add more from the first word
		if (prefix.length() < 2 && words.length > 0) {
			String firstWord = words[0];
			for (int i = 1; i < firstWord.length() && prefix.length() < MAX_PREFIX_LENGTH; i++) {
				if (Character.isLetterOrDigit(firstWord.charAt(i))) {
					prefix.append(firstWord.charAt(i));
				}
			}
		}

		// Ensure we have at least 2 characters
		if (prefix.length() < 2) {
			prefix.append("GEN");
		}

		return prefix.substring(0, Math.min(prefix.length(), MAX_PREFIX_LENGTH));
	}

	/**
	 * Generate random alphanumeric ID
	 *
	 * @return Random uppercase alphanumeric string
	 */
	public static String generateRandomId() {
		return UUID.randomUUID()
				.toString()
				.replace("-", "")
				.substring(0, RANDOM_ID_LENGTH)
				.toUpperCase();
	}

	/**
	 * Generate fallback certificate code when course title processing fails
	 *
	 * @param sequence
	 *            Sequential number
	 * @return Fallback certificate code
	 */
	private static String generateFallbackCode(int sequence) {
		String year = String.valueOf(LocalDateTime.now().getYear());
		String sequenceStr = String.format("%0" + SEQUENCE_DIGITS + "d", sequence);
		String randomId = generateRandomId();

		return String.format("GEN-%s-%s-%s", year, sequenceStr, randomId);
	}

	/**
	 * Check if a word is a common word that should be skipped in prefix generation
	 *
	 * @param word
	 *            The word to check
	 * @return true if the word is common and should be skipped
	 */
	private static boolean isCommonWord(String word) {
		String upperWord = word.toUpperCase();
		return upperWord.equals("THE")
				|| upperWord.equals("AND")
				|| upperWord.equals("OF")
				|| upperWord.equals("TO")
				|| upperWord.equals("A")
				|| upperWord.equals("AN")
				|| upperWord.equals("IN")
				|| upperWord.equals("FOR")
				|| upperWord.equals("WITH")
				|| upperWord.equals("ON")
				|| upperWord.equals("COURSE")
				|| upperWord.equals("TUTORIAL")
				|| upperWord.equals("GUIDE")
				|| upperWord.equals("INTRODUCTION")
				|| upperWord.equals("INTRO");
	}

	/**
	 * Validate certificate code format
	 *
	 * @param certificateCode
	 *            The code to validate
	 * @return true if the code follows the expected format
	 */
	public static boolean isValidCertificateCode(String certificateCode) {
		if (certificateCode == null || certificateCode.trim().isEmpty()) {
			return false;
		}

		// Pattern: PREFIX-YEAR-SEQUENCE-RANDOMID
		String pattern = "^[A-Z]{2,4}-\\d{4}-\\d{3}-[A-Z0-9]{8}$";
		return Pattern.matches(pattern, certificateCode.trim());
	}

	/**
	 * Extract course prefix from certificate code
	 *
	 * @param certificateCode
	 *            The certificate code
	 * @return Course prefix or null if invalid format
	 */
	public static String extractCoursePrefix(String certificateCode) {
		if (!isValidCertificateCode(certificateCode)) {
			return null;
		}

		String[] parts = certificateCode.split("-");
		return parts.length > 0 ? parts[0] : null;
	}

	/**
	 * Extract year from certificate code
	 *
	 * @param certificateCode
	 *            The certificate code
	 * @return Year as string or null if invalid format
	 */
	public static String extractYear(String certificateCode) {
		if (!isValidCertificateCode(certificateCode)) {
			return null;
		}

		String[] parts = certificateCode.split("-");
		return parts.length > 1 ? parts[1] : null;
	}
}
