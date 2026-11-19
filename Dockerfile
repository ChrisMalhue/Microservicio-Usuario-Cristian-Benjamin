# Etapa 1: Compilar el .jar con Gradle y Java 21
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle* settings.gradle* ./
COPY src src
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

# Etapa 2: Ejecutar el .jar con Java 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]