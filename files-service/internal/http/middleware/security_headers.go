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
		// Allow media/image delivery while keeping other surface areas locked down.
		// media-src/img-src are opened to any origin because files are fetched cross-site
		// from the frontend; default-src stays restrictive for everything else.
		c.Header("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; base-uri 'self'; img-src * data:; media-src * data: blob:")
		c.Next()
	}
}
