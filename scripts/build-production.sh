#!/bin/bash
# Build for production

set -e

echo "📦 Building production JAR..."
./mvnw clean package -DskipTests

echo "✅ Build complete: target/*.jar"