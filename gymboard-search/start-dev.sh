#!/usr/bin/env bash

# Starts the search application in development mode. Note that the database
# should be started via `docker-compose up` in the gymboard root dir.

echo "Starting gymboard-search development server."
./mvnw spring-boot:run -Dspring-boot.run.profiles=development
