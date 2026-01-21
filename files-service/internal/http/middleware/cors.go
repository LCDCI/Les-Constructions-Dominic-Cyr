package middleware

import (
	"time"

	"os"
	"strings"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
)

func CORSMiddleware() gin.HandlerFunc {
	// Read allowed origins from env (comma-separated), fallback to '*'
	allowed := os.Getenv("CORS_ALLOWED_ORIGINS")
	var origins []string
	if allowed == "" {
		origins = []string{"*"}
	} else {
		origins = strings.Split(allowed, ",")
	}
	// If using credentials, '*' is not allowed
	allowCreds := true
	if len(origins) == 1 && origins[0] == "*" {
		allowCreds = false
	}
	return cors.New(cors.Config{
		AllowOrigins:     origins,
		AllowMethods:     []string{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Origin", "Content-Type", "Accept", "Authorization", "Range"},
		ExposeHeaders:    []string{"Content-Length", "Content-Range", "Accept-Ranges"},
		AllowCredentials: allowCreds,
		MaxAge:           12 * time.Hour,
	})
}
