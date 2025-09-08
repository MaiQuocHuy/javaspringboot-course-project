package project.ktc.springboot_app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous processing
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * Task executor for affiliate payout processing
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("Creating async task executor for affiliate payouts");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("affiliate-payout-");
        executor.setRejectedExecutionHandler((r, executor1) -> {
            log.warn("Affiliate payout task rejected, executing synchronously");
            r.run();
        });
        executor.initialize();

        return executor;
    }
}
