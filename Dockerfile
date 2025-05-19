# Stage 1: Build the jar
FROM gradle:7.6-jdk17-alpine as builder

WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

# Stage 2: Run the jar
FROM eclipse-temurin:17-jdk-alpine

RUN apk add --no-cache bash gettext

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
COPY src/main/resources/application.properties .
COPY entrypoint.sh .

ENTRYPOINT ["sh", "entrypoint.sh"]