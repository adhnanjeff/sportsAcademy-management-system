# Multi-stage build for smaller image
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy maven files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/academy-management-0.0.1-SNAPSHOT.jar app.jar

# Expose port (will be overridden by PORT env var)
EXPOSE 8080

# Run with optimized settings for Render
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
