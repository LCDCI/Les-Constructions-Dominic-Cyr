FROM golang:1.22 AS build

WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN go build -o files-service ./cmd/files/main.go

FROM alpine
WORKDIR /app

COPY --from=build /app/files-service .

EXPOSE 8081
CMD ["./files-service"]
