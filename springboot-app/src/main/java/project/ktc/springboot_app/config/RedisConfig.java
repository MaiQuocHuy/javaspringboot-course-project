package project.ktc.springboot_app.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Configuration for Spring Boot application with Lettuce client
 * 
 * This configuration provides:
 * - Redis connection factory with Lettuce client
 * - Custom RedisTemplate with JSON serialization
 * - Cache manager with different TTL for different cache names
 * - Connection pooling configuration
 * 
 * @author KTC Team
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

        @Value("${spring.data.redis.host}")
        private String redisHost;

        @Value("${spring.data.redis.port}")
        private int redisPort;

        @Value("${spring.data.redis.password}")
        private String redisPassword;

        @Value("${spring.data.redis.database:0}")
        private int database;

        @Value("${spring.data.redis.timeout:2000ms}")
        private Duration timeout;

        @Value("${spring.data.redis.lettuce.shutdown-timeout:100ms}")
        private Duration shutdownTimeout;

        @Value("${spring.cache.redis.time-to-live:600000}")
        private long defaultTtlMs;

        @Value("${spring.cache.redis.key-prefix:ktc-cache:}")
        private String keyPrefix;

        @Value("${spring.cache.redis.cache-null-values:false}")
        private boolean cacheNullValues;

        /**
         * Redis Connection Factory using Lettuce with connection pooling
         */
        @Bean
        @Primary
        public RedisConnectionFactory redisConnectionFactory() {
                log.info("Configuring Redis connection to {}:{} with database {}", redisHost, redisPort, database);

                // Redis standalone configuration
                RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
                redisConfig.setHostName(redisHost);
                redisConfig.setPort(redisPort);
                redisConfig.setPassword(redisPassword);
                redisConfig.setDatabase(database);

                // Lettuce connection pooling configuration
                LettucePoolingClientConfiguration poolConfig = LettucePoolingClientConfiguration.builder()
                                .commandTimeout(timeout)
                                .shutdownTimeout(shutdownTimeout)
                                .build();

                LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, poolConfig);
                factory.setValidateConnection(true);

                return factory;
        }

        /**
         * Custom RedisTemplate with String keys and JSON value serialization
         */
        @Bean
        @Primary
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
                log.info("Configuring RedisTemplate with JSON serialization");

                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                // Configure JSON serializer
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.activateDefaultTyping(
                                objectMapper.getPolymorphicTypeValidator(),
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(
                                objectMapper);
                StringRedisSerializer stringSerializer = new StringRedisSerializer();

                // Set serializers
                template.setKeySerializer(stringSerializer);
                template.setValueSerializer(jsonSerializer);
                template.setHashKeySerializer(stringSerializer);
                template.setHashValueSerializer(jsonSerializer);

                template.setDefaultSerializer(jsonSerializer);
                template.afterPropertiesSet();

                return template;
        }

        /**
         * Cache Manager with different TTL configurations for different cache types
         */
        @Bean
        @Primary
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                log.info("Configuring Redis Cache Manager with custom TTL settings");

                // Default cache configuration
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMillis(defaultTtlMs)) // Use configurable default TTL
                                .prefixCacheNameWith(keyPrefix)
                                .serializeKeysWith(
                                                org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(
                                                org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                                                                .fromSerializer(createJsonSerializer()));

                // Configure null value caching based on properties
                if (!cacheNullValues) {
                        defaultConfig = defaultConfig.disableCachingNullValues();
                }

                // Configure different TTL for different cache names
                Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

                // Public data - longer TTL (1 hour)
                cacheConfigurations.put("courses", defaultConfig.entryTtl(Duration.ofHours(1)));
                cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofHours(2)));
                cacheConfigurations.put("course-details", defaultConfig.entryTtl(Duration.ofMinutes(30)));

                // Semi-dynamic data - medium TTL (15 minutes)
                cacheConfigurations.put("instructor-stats", defaultConfig.entryTtl(Duration.ofMinutes(15)));
                cacheConfigurations.put("course-stats", defaultConfig.entryTtl(Duration.ofMinutes(15)));
                cacheConfigurations.put("enrollment-stats", defaultConfig.entryTtl(Duration.ofMinutes(10)));

                // Metadata - longer TTL (30 minutes)
                cacheConfigurations.put("permissions", defaultConfig.entryTtl(Duration.ofMinutes(30)));
                cacheConfigurations.put("roles", defaultConfig.entryTtl(Duration.ofMinutes(30)));
                cacheConfigurations.put("user-profiles", defaultConfig.entryTtl(Duration.ofMinutes(20)));

                // Reviews and ratings - medium TTL (20 minutes)
                cacheConfigurations.put("course-reviews", defaultConfig.entryTtl(Duration.ofMinutes(20)));
                cacheConfigurations.put("instructor-reviews", defaultConfig.entryTtl(Duration.ofMinutes(20)));

                // Search results - shorter TTL (5 minutes)
                cacheConfigurations.put("search-results", defaultConfig.entryTtl(Duration.ofMinutes(5)));

                // User-specific data - shorter TTL (5 minutes)
                cacheConfigurations.put("user-courses", defaultConfig.entryTtl(Duration.ofMinutes(5)));
                cacheConfigurations.put("user-progress", defaultConfig.entryTtl(Duration.ofMinutes(5)));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .build();
        }

        /**
         * Create Jackson JSON serializer for Redis values
         */
        private GenericJackson2JsonRedisSerializer createJsonSerializer() {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.activateDefaultTyping(
                                objectMapper.getPolymorphicTypeValidator(),
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);
                return new GenericJackson2JsonRedisSerializer(objectMapper);
        }
}