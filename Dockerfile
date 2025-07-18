FROM eclipse-temurin:24-jdk

WORKDIR /app

COPY app.jar app.jar

CMD ["java", "-jar", "app.jar"]
