FROM golang:1.24 AS builder
WORKDIR /app/GoMailer
<<<<<<< HEAD
# When built via docker-compose, context is ./mailer-service so context root is this service
COPY . .
RUN ["go", "install", "github.com/swaggo/swag/cmd/swag@v1.3.2"]
RUN ["swag", "init", "-g", "./cmd/mailer/main.go", "-d", "./cmd/mailer,./internal,./pkg", "--swagger", "./docs", "--parseDependency", "--parseInternal"]
=======
# Build context is root directory, copy from mailer-service subdirectory
COPY mailer-service/ .
>>>>>>> 210cf4e617f8d32f55008978df942b84204bf612
RUN ["go", "mod", "download"]
RUN ["env", "CGO_ENABLED=0", "GOOS=linux", "go", "build", "-a", "-installsuffix", "cgo", "-o", "app", "./cmd/mailer"]

FROM scratch
COPY --from=builder /app/GoMailer/app .
EXPOSE 8080
ENV GIN_MODE release
ENTRYPOINT ["./app"]
