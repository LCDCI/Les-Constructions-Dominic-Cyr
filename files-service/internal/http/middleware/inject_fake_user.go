package middleware

import "github.com/gin-gonic/gin"

// temporary
func InjectFakeUser() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Set("userId", "demo-user-123")
		c.Next()
	}
}
