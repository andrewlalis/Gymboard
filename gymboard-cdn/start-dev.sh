#!/usr/bin/env bash

# Starts the CDN application in development mode. Note that the database
# should be started via `docker-compose up` in the gymboard root dir.

echo "Starting gymboard-cdn development server."
./mvnw spring-boot:run -Dspring-boot.run.profiles=development
