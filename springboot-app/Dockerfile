# ================================
# Multi-stage Docker build for Spring Boot Application with JDK 21
# ================================

# Stage 1: Build stage với Maven
FROM maven:3.9.9-amazoncorretto-21 AS build

# Thiết lập working directory
WORKDIR /app

# Copy các file cấu hình Maven trước để tận dụng Docker layer caching
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Download dependencies (tận dụng cache layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application và skip tests để build nhanh hơn
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage với JRE 21
FROM amazoncorretto:21-alpine-jdk

# Metadata
LABEL maintainer="KTC Team"
LABEL description="KTC Learning Platform - Spring Boot Application"
LABEL version="1.0.0"

# Tạo user không phải root để chạy application (security best practice)
RUN addgroup -g 1001 -S spring && \
    adduser -u 1001 -S spring -G spring

# Thiết lập working directory
WORKDIR /app

# Copy JAR file từ build stage
COPY --from=build /app/target/springboot-app-*.jar app.jar

# Tạo thư mục uploads và set permissions
RUN mkdir -p /app/uploads && \
    chown -R spring:spring /app

# Chuyển sang user spring
USER spring

# Expose port (Spring Boot default port)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Environment variables với default values
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-docker}
ENV JVM_OPTS="-Xmx512m -Xms256m"
ENV DB_URL=${DB_URL}
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}

# Entry point với JVM tuning
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]
