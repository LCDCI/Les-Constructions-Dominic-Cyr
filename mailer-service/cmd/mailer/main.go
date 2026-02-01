package main

import (
	"log"

	"github.com/gin-gonic/gin"
	ginSwagger "github.com/swaggo/gin-swagger"
	"github.com/swaggo/gin-swagger/swaggerFiles"

	"mailer-service/internal/http/handlers"
	"mailer-service/internal/http/middleware"
	mailsvc "mailer-service/internal/mailer"
	"mailer-service/internal/util"

	"github.com/prometheus/client_golang/prometheus/promhttp"
)

// @title Mailer Service API
// @version 1.0
// @description REST endpoints for sending emails.
// @BasePath /
func main() {
	r := gin.Default()

	r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))
	r.GET("/metrics", gin.WrapH(promhttp.Handler()))

	apiKey := util.Getenv("BREVO_API_KEY")
	fromEmail := util.Getenv("BREVO_FROM_EMAIL")
	if fromEmail == "" {
		fromEmail = util.GetenvOrDefault("SMTP_FROM", "noreply@constructions-dominiccyr.com")
	}

	svc := mailsvc.NewService(apiKey, fromEmail)

	h := handlers.NewMailHandler(svc)
	g := r.Group("/mail")
	g.Use(middleware.UnmarshalMail())
	g.POST("", h.Post)

	port := util.GetenvOrDefault("MAILER_PORT", "8083")
	
	log.Printf("Mailer Service using Brevo REST API on port %s", port)
	log.Printf("Sender email address: %s", fromEmail)

	if err := r.Run(":" + port); err != nil {
		log.Fatal(err)
	}
}
