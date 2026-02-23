# Stage 1: Build
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
RUN chmod +x gradlew

COPY build.gradle settings.gradle ./
COPY src/ src/

RUN sed -i "s/const BUILD_TIME = '0'/const BUILD_TIME = '$(date +%s)'/" src/main/resources/static/sw.js

RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd -r appuser && useradd -r -g appuser appuser

COPY --from=build /app/build/libs/*.jar app.jar

RUN chown appuser:appuser app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
