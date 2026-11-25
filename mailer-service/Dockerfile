FROM golang:1.25-alpine AS builder
WORKDIR /app/GoMailer
RUN apk add --no-cache ca-certificates
# Build context is root directory, copy from mailer-service subdirectory
COPY mailer-service/ .
RUN ["go", "mod", "download"]
RUN ["env", "CGO_ENABLED=0", "GOOS=linux", "go", "build", "-a", "-installsuffix", "cgo", "-o", "app", "./cmd/mailer"]

FROM scratch
COPY --from=builder /app/GoMailer/app .
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/ca-certificates.crt
EXPOSE 8080
ENV GIN_MODE=release
ENTRYPOINT ["./app"]