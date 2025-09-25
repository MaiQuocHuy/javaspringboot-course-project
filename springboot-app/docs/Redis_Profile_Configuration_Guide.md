# Redis Profile-based Configuration Guide

## Overview

This guide explains how the Redis configuration has been customized to support profile-based connections for local development and remote production environments.

## Architecture

The Redis configuration now supports two profiles:

### 1. Development Profile (`dev`)

- **Connection Factory**: `localRedisConnectionFactory()`
- **Target**: Local Redis instance
- **Features**:
  - Host: `localhost` (default)
  - Port: `6379` (default)
  - SSL: Disabled
  - Password: Optional (can be empty for local)
  - Timeouts: Optimized for fast development feedback
    - Command timeout: 1 second
    - Shutdown timeout: 100ms

### 2. Production Profile (`prod`)

- **Connection Factory**: `remoteRedisConnectionFactory()`
- **Target**: Remote Redis instance (e.g., Upstash)
- **Features**:
  - Host: From environment variables
  - Port: From environment variables
  - SSL: Enabled (configurable)
  - Password: Required from environment
  - Timeouts: Optimized for production stability
    - Command timeout: 10 seconds (configurable)
    - Shutdown timeout: 15 seconds

## Configuration Files

### application-dev.properties

```properties
# Local Redis server configuration - no SSL, default settings
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.password=${REDIS_PASSWORD:}
spring.data.redis.ssl.enabled=false
spring.data.redis.timeout=1000ms
spring.data.redis.database=0
spring.data.redis.connect-timeout=2000ms
```

### application-prod.properties

```properties
# Remote Redis server configuration - with SSL and optimized timeouts
spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}
spring.data.redis.password=${REDIS_PASSWORD}
spring.data.redis.ssl.enabled=${REDIS_SSL_ENABLED:true}
spring.data.redis.timeout=10000ms
spring.data.redis.database=0
spring.data.redis.connect-timeout=15000ms
upstash.redis.rest.url=${UPSTASH_REDIS_REST_URL}
```

## Code Implementation

### RedisConfig.java

The `RedisConfig` class now contains two `@Bean` methods with profile-specific annotations:

```java
@Bean
@Primary
@Profile("dev")
public RedisConnectionFactory localRedisConnectionFactory() {
    // Local development configuration
    // - Shorter timeouts for faster feedback
    // - No SSL requirement
    // - Optional password
}

@Bean
@Primary
@Profile("prod")
public RedisConnectionFactory remoteRedisConnectionFactory() {
    // Production configuration
    // - SSL support
    // - Longer timeouts for stability
    // - Required authentication
}
```

## Usage

### Running with Development Profile

```bash
java -jar app.jar --spring.profiles.active=dev
```

or set environment variable:

```bash
SPRING_PROFILES_ACTIVE=dev
```

### Running with Production Profile

```bash
java -jar app.jar --spring.profiles.active=prod
```

or set environment variable:

```bash
SPRING_PROFILES_ACTIVE=prod
```

## Environment Variables

### Development (Local Redis)

```bash
# Optional - defaults to localhost:6379
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=  # Can be empty
```

### Production (Remote Redis)

```bash
# Required for remote connection
REDIS_HOST=your-redis-host.com
REDIS_PORT=6379
REDIS_PASSWORD=your-secure-password
REDIS_SSL_ENABLED=true
UPSTASH_REDIS_REST_URL=https://your-upstash-url
```

## Benefits

1. **Environment Separation**: Clear separation between local development and production Redis instances
2. **Optimized Configuration**: Each profile has timeouts and settings optimized for its use case
3. **Security**: Production profile enforces SSL and authentication, development profile is simplified
4. **Flexibility**: Easy switching between environments using Spring profiles
5. **Maintainability**: Single configuration class manages both environments

## Testing Profile Configuration

You can verify which profile is active by checking the application logs:

### Development Profile Active:

```
INFO  - Configuring LOCAL Redis connection to localhost:6379 with database 0 (Profile: dev)
INFO  - Local Redis connection factory configured successfully
```

### Production Profile Active:

```
INFO  - Configuring REMOTE Redis connection to your-host:6379 with database 0 (SSL: true) (Profile: prod)
INFO  - SSL enabled for remote Redis connection
INFO  - Remote Redis connection factory configured successfully
```

## Troubleshooting

### Common Issues

1. **Profile Not Active**: Ensure `spring.profiles.active` is set correctly
2. **Connection Timeout**: Check if Redis server is accessible and firewall rules
3. **Authentication Failed**: Verify Redis password in environment variables
4. **SSL Issues**: Ensure SSL is properly configured for production Redis

### Debugging

Enable Redis debug logging:

```properties
logging.level.org.springframework.data.redis=DEBUG
logging.level.io.lettuce.core=DEBUG
```

This will show detailed connection information and help diagnose issues.
