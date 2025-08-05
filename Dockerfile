FROM openjdk:21-jdk-slim

WORKDIR /app

LABEL authors="glosona"

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

