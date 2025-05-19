#!/bin/sh

envsubst < application.properties > config.properties

exec java -jar app.jar --spring.config.location=file:/app/config.properties