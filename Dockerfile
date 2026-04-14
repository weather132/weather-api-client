# ── Build Stage ──
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

COPY src ./src
RUN gradle bootJar --no-daemon -x test

# ── Runtime Stage ──
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S climate && adduser -S climate -G climate

COPY --from=builder /app/build/libs/*.jar app.jar
COPY firebase-sa.json /app/firebase-sa.json

RUN chown climate:climate /app/firebase-sa.json
USER climate

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]