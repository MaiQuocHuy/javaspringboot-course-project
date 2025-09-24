package project.ktc.springboot_app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import project.ktc.springboot_app.cache.interfaces.CacheService;
import project.ktc.springboot_app.cache.services.infrastructure.HybridRedisCacheService;
import project.ktc.springboot_app.cache.services.infrastructure.UpstashRedisRestService;

/**
 * Configuration class to ensure proper bean setup for cache services.
 * This ensures that HybridRedisCacheService gets all its dependencies properly
 * injected.
 * 
 * @author KTC Team
 */
@Slf4j
@Configuration
public class CacheServiceConfig {

    @Bean
    @Primary
    public CacheService hybridCacheService(
            RedisTemplate<String, Object> redisTemplate,
            UpstashRedisRestService upstashRedisRestService,
            ObjectMapper objectMapper) {

        log.info("Configuring HybridRedisCacheService as primary cache service");
        return new HybridRedisCacheService(redisTemplate, upstashRedisRestService, objectMapper);
    }

    @Bean
    @Primary
    public ObjectMapper cacheObjectMapper() {
        log.info("Configuring ObjectMapper for cache serialization");
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // This will register JavaTimeModule and other modules
        return mapper;
    }
}
