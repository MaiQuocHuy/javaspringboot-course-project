# Cache Services Organization Structure

## Overview

The cache services have been reorganized into a clean, domain-driven architecture that separates concerns and improves maintainability.

## New Folder Structure

```
src/main/java/project/ktc/springboot_app/cache/
├── dto/                                    # Cache DTOs
│   ├── InstructorStatisticsCacheDto.java
│   └── ...
├── interfaces/                             # Cache service interfaces
│   └── CacheService.java
├── keys/                                   # Cache key management
│   ├── CacheConstants.java
│   └── CacheKeyBuilder.java
├── mappers/                                # DTO conversion utilities
│   ├── InstructorStatisticsCacheMapper.java
│   ├── CategoryCacheMapper.java
│   ├── ReviewCacheMapper.java
│   └── ...
├── services/
│   ├── domain/                             # Domain-specific cache services
│   │   ├── InstructorStatisticsCacheService.java    # 📍 Instructor statistics caching
│   │   ├── CoursesCacheService.java                 # Course-related caching
│   │   ├── ReviewsCacheService.java                 # Review-related caching
│   │   └── CategoryCacheService.java                # Category-related caching
│   ├── infrastructure/                     # Infrastructure cache services
│   │   ├── RedisCacheServiceImp.java               # Redis implementation
│   │   └── CacheInvalidationService.java            # Cross-domain cache invalidation
│   └── CacheStats.java                     # Cache statistics
```

## Service Layer Separation

### Domain Services (`services/domain/`)

**Purpose**: Business domain-specific cache operations
**Characteristics**:

- High-level cache operations for specific business domains
- Use cache mappers for DTO conversion
- Handle domain-specific cache invalidation patterns
- Use the CacheService interface for abstraction

**Examples**:

- `InstructorStatisticsCacheService`: Instructor dashboard statistics
- `CoursesCacheService`: Course listings, details, search results
- `ReviewsCacheService`: Course reviews and ratings
- `CategoryCacheService`: Category data

### Infrastructure Services (`services/infrastructure/`)

**Purpose**: Technical infrastructure and cross-cutting concerns
**Characteristics**:

- Low-level cache implementation details
- Cross-domain operations (invalidation, monitoring)
- Infrastructure abstractions

**Examples**:

- `RedisCacheServiceImp`: Redis-specific implementation
- `CacheInvalidationService`: Cross-domain cache invalidation

## Design Principles

### 1. Domain-Driven Design

- Each domain service handles cache operations for a specific business area
- Clear separation between business logic and infrastructure concerns

### 2. Clean Architecture

- **Domain Layer**: Business-specific cache operations
- **Infrastructure Layer**: Technical implementation details
- **Interface Layer**: Abstract cache contracts

### 3. Single Responsibility

- Each service has a clear, focused responsibility
- Infrastructure concerns separated from business logic

### 4. Dependency Direction

```
Domain Services → Interface → Infrastructure Services
     ↓
Cache Mappers → Cache DTOs
```

## Benefits of New Structure

### 1. **Improved Maintainability**

- Clear separation of concerns
- Easy to locate domain-specific cache logic
- Infrastructure changes don't affect domain services

### 2. **Better Testability**

- Domain services can be tested independently
- Mock infrastructure services easily
- Clear boundaries for unit testing

### 3. **Enhanced Scalability**

- Easy to add new domain-specific cache services
- Infrastructure services can be enhanced without affecting domains
- Clear patterns for new team members

### 4. **Consistent Patterns**

- All domain services follow the same structure
- Consistent error handling and logging
- Standardized cache key management

## Migration Impact

### Updated Imports

The following imports have been updated:

```java
// OLD
import project.ktc.springboot_app.cache.services.InstructorStatisticsCacheService;

// NEW
import project.ktc.springboot_app.cache.services.domain.InstructorStatisticsCacheService;
```

### Service Dependencies

Services now inject dependencies from the appropriate layers:

- Domain services use other domain services and infrastructure interfaces
- Infrastructure services implement interfaces and provide cross-cutting concerns

## Cache Key Management

### Centralized Key Building

All cache keys are built through `CacheKeyBuilder` with consistent patterns:

```java
// Instructor statistics
instructor_statistics:instructor:{instructor-id}

// Course details
courses:detail:{course-id}

// Reviews
reviews:course:{course-id}:page:{page}:size:{size}:sort:{sort}
```

### Invalidation Patterns

Consistent invalidation patterns for each domain:

```java
// Instructor statistics
INSTRUCTOR_STATISTICS_INVALIDATION_PATTERN = "instructor_statistics:instructor:%s"

// Course reviews
COURSE_REVIEWS_INVALIDATION_PATTERN = "reviews:course:%s:*"
```

## Implementation Guidelines

### Adding New Domain Cache Service

1. Create service in `services/domain/`
2. Follow existing patterns (error handling, logging, mapper usage)
3. Add appropriate cache constants and key builders
4. Create domain-specific DTOs and mappers
5. Add invalidation methods to `CacheInvalidationService`

### Adding Infrastructure Service

1. Create service in `services/infrastructure/`
2. Implement appropriate interfaces
3. Focus on technical concerns, not business logic
4. Provide cross-cutting functionality

This structure provides a solid foundation for scalable, maintainable cache management while keeping the codebase organized and consistent.
