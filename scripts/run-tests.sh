#!/bin/bash
# Run tests with test profile

set -e

echo "🧪 Loading test environment..."
# export $(cat .env.test | xargs)

echo "📦 Running tests with test profile..."
./mvnw clean test -Dspring.profiles.active=test