FROM golang:1.24 AS builder
WORKDIR /app/GoMailer
# Build context is repo root (source_dir: /), so copy only mailer-service so go.mod is in WORKDIR
COPY mailer-service/ .
RUN ["go", "mod", "download"]
RUN ["env", "CGO_ENABLED=0", "GOOS=linux", "go", "build", "-a", "-installsuffix", "cgo", "-o", "app", "./cmd/mailer"]

FROM scratch
COPY --from=builder /app/GoMailer/app .
EXPOSE 8080
ENV GIN_MODE release
ENTRYPOINT ["./app"]
