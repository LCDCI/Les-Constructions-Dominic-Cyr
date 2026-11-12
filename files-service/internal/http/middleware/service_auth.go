package middleware

import (
	"crypto/subtle"
	"net/http"
	"os"

	"github.com/gin-gonic/gin"
)

// RequireServiceKey protects the file service from direct unauthenticated access.
// The key must be supplied by a trusted backend or gateway, never by browser code.
func RequireServiceKey() gin.HandlerFunc {
	expected := os.Getenv("FILES_SERVICE_API_KEY")
	return func(c *gin.Context) {
		provided := c.GetHeader("X-Internal-Service-Key")
		if expected == "" || provided == "" ||
			subtle.ConstantTimeCompare([]byte(provided), []byte(expected)) != 1 {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "authentication required"})
			return
		}
		c.Next()
	}
}
