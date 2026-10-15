# ==========================================
# Stage 1: Build & Package (Builder Stage)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Cache Maven dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Hardened Lightweight Runtime Stage
# ==========================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# Security Best Practice: Create non-root system user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy compiled JAR from builder stage
COPY --from=builder /build/target/loan-application-tracking-portal-1.0.0-SNAPSHOT.jar app.jar

# Enforce non-root ownership
RUN chown -R appuser:appgroup /app
USER appuser

# Expose Loan Portal port
EXPOSE 8081

# Docker Healthcheck targeting Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD wget -q --spider http://localhost:8081/actuator/health || exit 1

# Launch Application
ENTRYPOINT ["java", "-jar", "app.jar"]
