package project.ktc.springboot_app.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class StringUtil {

    public String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }

        return name.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters except spaces and hyphens
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
    }

    public String getBeforeDot(String input) {
        if (input == null)
            return null;
        int dotIndex = input.indexOf('.');
        if (dotIndex != -1) {
            return input.substring(0, dotIndex);
        }
        return input; // Không có dấu .
    }
}
