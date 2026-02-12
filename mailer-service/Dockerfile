FROM golang:1.24 AS builder
WORKDIR /app/GoMailer
# Copy dependency files first for better layer caching
COPY go.mod go.sum* ./
RUN go mod download
# Copy source code
COPY . .
RUN go install github.com/swaggo/swag/cmd/swag@v1.3.2
RUN swag init -g ./cmd/mailer/main.go -d ./cmd/mailer,./internal,./pkg --swagger ./docs --parseDependency --parseInternal
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o app ./cmd/mailer

FROM scratch
COPY --from=builder /app/GoMailer/app .
EXPOSE 8080
ENV GIN_MODE release
ENTRYPOINT ["./app"]
