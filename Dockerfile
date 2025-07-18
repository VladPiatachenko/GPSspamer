# Use a lightweight OpenJDK base image
FROM eclipse-temurin:24-jdk

# Create app directory
WORKDIR /app

# Copy the built jar into the container
COPY target/*.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
