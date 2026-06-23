#!/bin/bash
# Start local development environment

set -e

echo "📝 Loading local environment variables from .env.local..."
if [ -f .env.local ]; then
    # Load env vars but ignore comments and empty lines
    export $(grep -v '^#' .env.local | xargs)
else
    echo "⚠️ .env.local not found, using default fallbacks."
fi

echo "🚀 Starting PostgreSQL with Docker Compose..."
docker compose --env-file .env.local up -d

echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 5

echo "📦 Starting Spring Boot with local profile..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
