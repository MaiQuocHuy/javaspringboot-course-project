package project.ktc.springboot_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration for Spring Boot Scheduling Enables scheduled tasks and configures the task
 * scheduler for payout processing
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

  /**
   * Configure custom task scheduler for scheduled jobs This allows better control over thread pool
   * size and naming for payout operations
   */
  @Bean
  public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(
        8); // Number of threads for scheduled tasks (increased for payout processing)
    scheduler.setThreadNamePrefix("payout-scheduler-");
    scheduler.setAwaitTerminationSeconds(120); // Longer wait for payout operations
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    scheduler.initialize();
    return scheduler;
  }
}
