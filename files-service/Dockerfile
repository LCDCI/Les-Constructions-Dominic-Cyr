FROM golang:1.24-alpine

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

RUN go get github.com/gin-contrib/cors
RUN go mod tidy

COPY . .

# Copy migrations directory for runtime access
COPY migrations ./migrations

RUN go build -o file-service ./cmd

EXPOSE 8080

CMD ["./file-service"]