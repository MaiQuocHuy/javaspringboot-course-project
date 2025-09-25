package project.ktc.springboot_app.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder;
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

        @Value("${spring.data.redis.ssl.enabled:false}")
        private boolean sslEnabled;

        /**
         * Local Redis Connection Factory for development profile
         * - Uses local Redis server without SSL
         * - Optimized for development with shorter timeouts
         */
        @Bean("redisConnectionFactory")
        @Profile("dev")
        public RedisConnectionFactory localRedisConnectionFactory() {
                log.info("Configuring LOCAL Redis connection to {}:{} with database {} (Profile: dev)",
                                redisHost, redisPort, database);

                // Redis standalone configuration for local
                RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
                redisConfig.setHostName(redisHost);
                redisConfig.setPort(redisPort);

                // For local dev, password might be empty or simple
                if (redisPassword != null && !redisPassword.trim().isEmpty()) {
                        redisConfig.setPassword(redisPassword);
                }
                redisConfig.setDatabase(database);

                // Local development configuration - shorter timeouts for faster feedback
                LettucePoolingClientConfiguration poolConfig = LettucePoolingClientConfiguration.builder()
                                .commandTimeout(Duration.ofSeconds(1)) // Shorter timeout for local
                                .shutdownTimeout(Duration.ofMillis(100))
                                .build();

                LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig,
                                poolConfig);
                factory.setValidateConnection(true);

                log.info("Local Redis connection factory configured successfully");
                return factory;
        }

        @Bean("redisConnectionFactory")
        @Profile("prod")
        public RedisConnectionFactory remoteRedisConnectionFactory() {
                log.info("Configuring LOCAL Redis connection to {}:{} with database {} (Profile: dev)",
                                redisHost, redisPort, database);

                // Redis standalone configuration for remote
                RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
                redisConfig.setHostName(redisHost);
                redisConfig.setPort(redisPort);

                // Set password if provided
                if (redisPassword != null && !redisPassword.trim().isEmpty()) {
                        redisConfig.setPassword(redisPassword);
                }
                redisConfig.setDatabase(database);

                // Client configuration builder
                LettuceClientConfiguration.LettuceClientConfigurationBuilder clientBuilder = LettuceClientConfiguration
                                .builder();

                // Configure SSL if enabled
                if (sslEnabled) {
                        log.info("SSL enabled for Redis connection");
                        clientBuilder.useSsl().disablePeerVerification(); // Important for DigitalOcean managed
                                                                          // databases
                }

                // Configure timeouts - longer for remote connections
                clientBuilder.commandTimeout(Duration.ofSeconds(5))
                                .shutdownTimeout(Duration.ofMillis(200));

                // Note: Connection pooling is configured at LettuceConnectionFactory level
                // The pooling is handled by Lettuce's internal connection pooling

                LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, clientBuilder.build());
                factory.setValidateConnection(true);

                log.info("Redis connection factory configured successfully with SSL: {}", sslEnabled);
                return factory;
        }

        /**
         * Remote Redis Connection Factory for production profile
         * - Uses remote Redis server with SSL support
         * - Optimized for production with longer timeouts and SSL
         */
        // @Bean("redisConnectionFactory")
        // @Profile("prod")
        // public RedisConnectionFactory remoteRedisConnectionFactory() {
        // log.info("Configuring REMOTE Redis connection to {}:{} with database {} (SSL:
        // {}) (Profile: prod)",
        // redisHost, redisPort, database, sslEnabled);

        // // Redis standalone configuration for remote
        // RedisStandaloneConfiguration redisConfig = new
        // RedisStandaloneConfiguration();
        // redisConfig.setHostName(redisHost);
        // redisConfig.setPort(redisPort);
        // redisConfig.setPassword(redisPassword);
        // redisConfig.setDatabase(database);

        // // Production configuration with SSL support
        // LettucePoolingClientConfigurationBuilder poolConfigBuilder =
        // LettucePoolingClientConfiguration.builder()
        // .commandTimeout(timeout) // Use configured timeout
        // .shutdownTimeout(shutdownTimeout);

        // // Enable SSL for production
        // if (sslEnabled) {
        // poolConfigBuilder.useSsl();
        // log.info("SSL enabled for remote Redis connection");
        // }

        // LettucePoolingClientConfiguration poolConfig = poolConfigBuilder.build();

        // LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig,
        // poolConfig);
        // factory.setValidateConnection(true);

        // log.info("Remote Redis connection factory configured successfully");
        // return factory;
        // }

        /**
         * Custom RedisTemplate with String keys and JSON value serialization
         */
        @Bean
        @Primary
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
                log.info("Configuring RedisTemplate with JSON serialization");

                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                // Configure JSON serializer using the main ObjectMapper
                ObjectMapper objectMapper = createConfiguredObjectMapper();
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
                ObjectMapper objectMapper = createConfiguredObjectMapper();
                objectMapper.activateDefaultTyping(
                                objectMapper.getPolymorphicTypeValidator(),
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);
                return new GenericJackson2JsonRedisSerializer(objectMapper);
        }

        /**
         * Create ObjectMapper with consistent LocalDateTime configuration matching
         * JacksonConfig
         */
        private ObjectMapper createConfiguredObjectMapper() {
                ObjectMapper objectMapper = new ObjectMapper();

                // Configure JavaTimeModule with same custom serializers as JacksonConfig
                JavaTimeModule javaTimeModule = new JavaTimeModule();
                javaTimeModule.addSerializer(LocalDateTime.class,
                                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                javaTimeModule.addSerializer(LocalDate.class,
                                new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                javaTimeModule.addSerializer(LocalTime.class,
                                new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));

                objectMapper.registerModule(javaTimeModule);
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

                return objectMapper;
        }

}