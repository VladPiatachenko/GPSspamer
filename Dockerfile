# Stage 1: build the jar
FROM eclipse-temurin:24-jdk AS builder
WORKDIR /build
COPY . .
RUN ./mvnw clean package -DskipTests

# Stage 2: run the jar
FROM eclipse-temurin:24-jdk
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
