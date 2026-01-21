



############################################################
# 1) BACKEND BUILD (Spring Boot)
############################################################
FROM gradle:8.5-jdk21 AS backend-build
WORKDIR /app

# Copy entire project (so Gradle sees build.gradle + src/)
COPY . .

# Build Spring Boot JAR
RUN gradle clean build -x test --no-daemon

############################################################
# 2) FRONTEND BUILD (Node)
############################################################
FROM node:20 AS frontend-build
WORKDIR /frontend

# Copy frontend source
COPY frontend/les_constructions_dominic_cyr ./

# Install dependencies and build
RUN npm install && npm run build

############################################################
# 3) FILES-SERVICE BUILD (Go)
############################################################
FROM golang:1.24-alpine AS files-service-build
WORKDIR /files-service

# Copy go module files
COPY files-service/go.mod files-service/go.sum* ./
RUN go mod download || true

# Copy source and migrations
COPY files-service/ ./
RUN go mod tidy

# Build the binary
RUN go build -o file-service ./cmd

############################################################
# 4) FINAL RUNTIME IMAGE (nginx + backend + files-service)
############################################################
FROM eclipse-temurin:21-jdk AS final
WORKDIR /app

# Copy backend JAR
COPY --from=backend-build /app/build/libs/les_constructions_dominic_cyr-0.0.1-SNAPSHOT.jar /app/app.jar

# Copy files-service binary and migrations
COPY --from=files-service-build /files-service/file-service /app/file-service
COPY --from=files-service-build /files-service/migrations /app/migrations

# Install nginx
RUN apt-get update && apt-get install -y nginx && rm -rf /var/lib/apt/lists/*

# Remove default nginx config and copy our config for COMBINED deployment
RUN rm -f /etc/nginx/sites-enabled/default
COPY --from=frontend-build /frontend/dist/ /usr/share/nginx/html/
COPY frontend/les_constructions_dominic_cyr/nginx/default-combined.conf /etc/nginx/conf.d/default.conf

# Expose ports
EXPOSE 8080 8082 80

# Create startup script to run nginx, backend, and files-service
RUN echo '#!/bin/bash\n\
set -e\n\
# Start nginx in background\n\
nginx\n\
# Start files-service in background\n\
cd /app && ./file-service &\n\
# Start backend (foreground)\n\
exec java -jar /app/app.jar' > /start.sh && chmod +x /start.sh

# Start all services
CMD ["/start.sh"]