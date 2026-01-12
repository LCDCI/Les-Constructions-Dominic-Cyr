package main

import (
	"log"

	"files-service/internal/config"
	"files-service/internal/http/handlers"
	"files-service/internal/http/middleware"
	"files-service/internal/repository"
	"files-service/internal/service"
	"files-service/internal/storage"

	"github.com/gin-gonic/gin"
)

func main() {

	cfg := config.Load()

	db := config.InitPostgres(cfg)
	defer db.Close()
	minioClient := storage.NewMinioClient(cfg)

	repo := repository.NewFileRepository(db)
	fileService := service.NewFileService(repo, minioClient)
	fileController := handlers.NewFileController(fileService)

	r := gin.Default()

	r.Use(middleware.RateLimiter(cfg))
	r.Use(middleware.CORSMiddleware())

	r.Use(middleware.ErrorHandler)
	r.Use(middleware.InjectFakeUser())

	fileController.RegisterRoutes(r)

	log.Println("File-service running on port 8080")
	r.Run(":8080")
}
