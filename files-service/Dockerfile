
FROM golang:1.24-alpine

WORKDIR /app

COPY go.mod ./
RUN go mod download || true

COPY . .

RUN go mod tidy

# Copy migrations directory for runtime access
COPY migrations ./migrations

RUN go build -o file-service ./cmd

EXPOSE 8080

CMD ["./file-service"]