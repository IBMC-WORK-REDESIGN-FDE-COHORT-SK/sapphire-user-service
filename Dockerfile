# ============================
# Stage 1 – Build the application
# ============================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B


# ============================
# Stage 2 – Runtime image
# ============================
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Sapphire User Service"
LABEL description="User service with Keycloak authentication and OpenTelemetry instrumentation"
LABEL version="1.0.0"

# Install glibc compatibility and curl
RUN apk add --no-cache gcompat libc6-compat curl

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Parameterized OpenTelemetry version
ARG OTEL_VERSION=2.2.0

# Download OTEL Java Agent
RUN curl -L -o /app/opentelemetry-javaagent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_VERSION}/opentelemetry-javaagent.jar

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8091

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8091/actuator/health || exit 1

# Run the service with OpenTelemetry
ENTRYPOINT ["sh", "-c", "java -javaagent:opentelemetry-javaagent.jar $JAVA_OPTS -jar app.jar"]