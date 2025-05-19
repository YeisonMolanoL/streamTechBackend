FROM eclipse-temurin:17-jdk-alpine

RUN apk add --no-cache bash gettext

WORKDIR /app

COPY build/libs/*.jar app.jar
COPY src/main/resources/application.properties .
COPY entrypoint.sh .

RUN chmod +x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]
