



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

# Build arguments for Vite environment variables
ARG VITE_API_BASE
ARG VITE_API_BASE_URL
ARG VITE_AUTH0_DOMAIN
ARG VITE_AUTH0_CLIENT_ID
ARG VITE_AUTH0_AUDIENCE
ARG VITE_SESSION_IDLE_MINUTES
ARG VITE_GA_MEASUREMENT_ID
ARG VITE_FILES_SERVICE_URL

# Make them available as environment variables during build
ENV VITE_API_BASE=$VITE_API_BASE
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_AUTH0_DOMAIN=$VITE_AUTH0_DOMAIN
ENV VITE_AUTH0_CLIENT_ID=$VITE_AUTH0_CLIENT_ID
ENV VITE_AUTH0_AUDIENCE=$VITE_AUTH0_AUDIENCE
ENV VITE_SESSION_IDLE_MINUTES=$VITE_SESSION_IDLE_MINUTES
ENV VITE_GA_MEASUREMENT_ID=$VITE_GA_MEASUREMENT_ID
ENV VITE_FILES_SERVICE_URL=$VITE_FILES_SERVICE_URL

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

# Expose ports
EXPOSE 8080 8082

# Create startup script to run backend and files-service
RUN echo '#!/bin/bash\n\
set -e\n\
# Start files-service in background\n\
cd /app && ./file-service &\n\
# Start backend (foreground)\n\
exec java -jar /app/app.jar' > /start.sh && chmod +x /start.sh

# Start all services
ENTRYPOINT ["/start.sh"]
CMD ["/start.sh"]