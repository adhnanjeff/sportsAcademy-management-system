# Use official Java 17 image
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy everything
COPY . .

# Build the application
RUN ./mvnw clean package -DskipTests

# Run the jar
CMD ["java", "-jar", "target/academy-management-0.0.1-SNAPSHOT.jar"]
