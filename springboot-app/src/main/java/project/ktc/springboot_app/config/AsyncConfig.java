package project.ktc.springboot_app.config;

import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration for asynchronous processing Optimized for payment background processing to prevent
 * webhook timeouts
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

  /**
   * Task executor for payment background processing (emails, notifications, affiliate payouts)
   * Optimized for handling heavy operations after payment completion
   */
  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    log.info("✅ Creating optimized async task executor for payment processing");

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // Increased pool size for better payment processing performance
    executor.setCorePoolSize(3);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("PaymentAsync-");
    executor.setKeepAliveSeconds(60);
    executor.setAllowCoreThreadTimeOut(true);

    executor.setRejectedExecutionHandler(
        (r, executor1) -> {
          log.warn(
              "⚠️ Payment background task rejected - queue full. Executing synchronously as fallback");
          r.run();
        });

    // Graceful shutdown for payment processing
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);

    executor.initialize();

    log.info(
        "🚀 Payment async executor ready: core={}, max={}, queue={}",
        executor.getCorePoolSize(),
        executor.getMaxPoolSize(),
        executor.getQueueCapacity());

    return executor;
  }
}
