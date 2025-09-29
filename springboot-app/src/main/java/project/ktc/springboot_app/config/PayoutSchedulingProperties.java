package project.ktc.springboot_app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Configuration properties for automated payout scheduling
 * Maps properties from application.properties with prefix "app.payout"
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.payout")
public class PayoutSchedulingProperties {

    /**
     * Scheduling configuration
     */
    private Scheduling scheduling = new Scheduling();

    /**
     * Waiting period configuration
     */
    private Waiting waiting = new Waiting();

    /**
     * Instructor earning configuration
     */
    private Instructor instructor = new Instructor();

    /**
     * Batch processing configuration
     */
    private int batchSize = 50;

    /**
     * Retry configuration
     */
    private Retry retry = new Retry();

    /**
     * Notification configuration
     */
    private Notification notification = new Notification();

    @Data
    public static class Scheduling {
        private boolean enabled = true;
    }

    @Data
    public static class Waiting {
        private Period period = new Period();

        @Data
        public static class Period {
            private int days = 3;
        }
    }

    @Data
    public static class Instructor {
        private Earning earning = new Earning();

        @Data
        public static class Earning {
            private BigDecimal percentage = new BigDecimal("0.70");
        }
    }

    @Data
    public static class Retry {
        private Max max = new Max();

        @Data
        public static class Max {
            private int attempts = 3;
        }
    }

    @Data
    public static class Notification {
        private boolean enabled = true;
        private Admin admin = new Admin();

        @Data
        public static class Admin {
            private List<String> emails = List.of("admin@ktc-learning.com", "maiquochuy16122003@gmail.com");
        }
    }
}

