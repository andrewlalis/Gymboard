#!/usr/bin/env bash

# Starts the api application in development mode. Note that the database
# should be started via `docker-compose up` in the gymboard root dir.

echo "Starting gymboard-api development server."
./gen_keys.d
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=development
