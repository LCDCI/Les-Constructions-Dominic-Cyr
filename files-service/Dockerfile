# ========================
# 1) Build stage
# ========================
FROM golang:1.22 AS build

WORKDIR /app

# Install required binaries
RUN go install github.com/swaggo/swag/cmd/swag@v1.8.12

# Copy Go module files
COPY go.mod go.sum ./
RUN go mod download

# Copy the rest of the service
COPY . .

# Generate swagger docs
RUN swag init -g ./cmd/files/main.go -d .

# Build service binary
RUN go build -o files-service ./cmd/files

# ========================
# 2) Runtime stage
# ========================
FROM debian:stable-slim

WORKDIR /app

# Create directory for file storage
RUN mkdir -p /app/files-data

COPY --from=build /app/files-service /app/files-service
COPY --from=build /app/docs /app/docs

EXPOSE 8082

CMD ["./files-service"]
