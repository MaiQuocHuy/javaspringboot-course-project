package project.ktc.springboot_app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import project.ktc.springboot_app.cache.interfaces.CacheService;
import project.ktc.springboot_app.cache.services.infrastructure.HybridRedisCacheService;
import project.ktc.springboot_app.cache.services.infrastructure.RedisCacheServiceImp;
import project.ktc.springboot_app.cache.services.infrastructure.UpstashRedisRestService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Unified Cache Service Configuration
 * 
 * Features:
 * - Profile-based CacheService selection (dev vs prod)
 * - Consistent ObjectMapper configuration for cache serialization
 * - Clean separation of concerns between local and hybrid cache strategies
 * - Reusable and maintainable code structure
 * 
 * @author KTC Team
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CacheServiceConfigUnified {

    /**
     * Development Profile - Local Redis Cache Service
     * - Uses RedisCacheServiceImp for simple local Redis operations
     * - No fallback mechanisms needed for development
     */
    @Bean("cacheService")
    @Primary
    @Profile("dev")
    public CacheService developmentCacheService(RedisCacheServiceImp redisCacheServiceImp) {
        log.info("🔧 Configuring DEV CacheService: RedisCacheServiceImp for local Redis");
        return redisCacheServiceImp;
    }

    /**
     * Production Profile - Hybrid Cache Service
     * - Uses HybridRedisCacheService with both Redis and Upstash REST fallback
     * - Provides high availability and fault tolerance in production
     */
    // @Bean("cacheService")
    // @Primary
    // @Profile("prod")
    // public CacheService productionCacheService(
    // RedisTemplate<String, Object> redisTemplate,
    // UpstashRedisRestService upstashRedisRestService,
    // @Qualifier("cacheObjectMapper") ObjectMapper cacheObjectMapper) {

    // log.info("🚀 Configuring PROD CacheService: HybridRedisCacheService with
    // Redis + Upstash REST fallback");
    // return new HybridRedisCacheService(redisTemplate, upstashRedisRestService,
    // cacheObjectMapper);
    // }

    @Bean("cacheService")
    @Primary
    @Profile("prod")
    public CacheService productionCacheService(RedisCacheServiceImp redisCacheServiceImp) {
        log.info("🔧 Configuring DEV CacheService: RedisCacheServiceImp for local Redis");
        return redisCacheServiceImp;
    }

    /**
     * Optimized ObjectMapper for Cache Serialization
     * - Consistent DateTime formatting matching JacksonConfig
     * - Proper timezone handling
     * - Module registration for Java 8 time types
     */
    @Bean("cacheObjectMapper")
    public ObjectMapper cacheObjectMapper() {
        log.info("⚙️  Configuring specialized ObjectMapper for cache serialization");

        ObjectMapper mapper = new ObjectMapper();

        // Configure JavaTimeModule with consistent formatting
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));

        mapper.registerModule(javaTimeModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        // Auto-discover and register other modules (Jackson modules)
        mapper.findAndRegisterModules();

        log.info("✅ Cache ObjectMapper configured with consistent DateTime formatting");
        return mapper;
    }
}