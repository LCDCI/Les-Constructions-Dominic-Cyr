package middleware

import (
	"errors"
	"files-service/internal/domain"
	"net/http"

	"github.com/gin-gonic/gin"
)

func ErrorHandler(c *gin.Context) {
	c.Next()

	if len(c.Errors) == 0 {
		return
	}

	err := c.Errors.Last().Err

	// If response already started, stop
	if c.Writer.Written() {
		return
	}

	// Domain errors
	if errors.Is(err, domain.ErrNotFound) {
		c.AbortWithStatusJSON(http.StatusNotFound, gin.H{"error": "resource not found"})
		return
	}

	if errors.Is(err, domain.ErrValidation) {
		c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	if errors.Is(err, domain.ErrStorageFailed) {
		c.AbortWithStatusJSON(http.StatusServiceUnavailable, gin.H{"error": "storage error"})
		return
	}

	// Generic internal error
	c.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{
		"error": "internal server error",
	})
}
