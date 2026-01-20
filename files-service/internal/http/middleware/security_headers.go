package middleware

import "github.com/gin-gonic/gin"

// SecurityHeaders adds a small set of defensive headers for clickjacking,
// MIME sniffing, basic XSS protections, and a constrained CSP for this API.
func SecurityHeaders() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Header("X-Content-Type-Options", "nosniff")
		c.Header("X-Frame-Options", "DENY")
		c.Header("X-XSS-Protection", "1; mode=block")
		c.Header("Referrer-Policy", "no-referrer")
		c.Header("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; base-uri 'self'")
		c.Next()
	}
}
