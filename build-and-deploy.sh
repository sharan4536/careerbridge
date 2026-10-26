#!/bin/bash
echo "Building CareerBridge Frontend and Backend..."

# 1. Build the React frontend
echo "Building React app..."
cd frontend
npm run build
cd ..

# 2. Copy the built React app to Spring Boot static resources
echo "Copying to Spring Boot static resources..."
mkdir -p src/main/resources/static
rm -rf src/main/resources/static/*
cp -R frontend/dist/* src/main/resources/static/

# 3. Build the Spring Boot Application
echo "Building Spring Boot Application..."
./mvnw clean package -DskipTests

echo "==================================="
echo "Build complete! You can run the application with:"
echo "java -jar target/careerbridge-0.0.1-SNAPSHOT.jar"
echo "==================================="
