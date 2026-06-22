#!/bin/bash
# Start local development environment

set -e

echo "🚀 Starting PostgreSQL with Docker..."
docker compose up -d

echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 5

echo "📦 Starting Spring Boot with local profile..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
