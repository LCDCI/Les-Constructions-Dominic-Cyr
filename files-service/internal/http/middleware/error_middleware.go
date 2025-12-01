package middleware

import (
	"errors"
	"files-service/internal/domain"
	"net/http"

	"github.com/gin-gonic/gin"
)

func ErrorHandler(c *gin.Context) {
	c.Next()

	if len(c.Errors) > 0 {
		err := c.Errors.Last().Err

		if c.Writer.Written() {
			return
		}

		var dErr *domain.DomainError

		if errors.As(err, &dErr) {

			switch dErr {
			case domain.ErrNotFound:
				c.AbortWithStatusJSON(http.StatusNotFound, gin.H{
					"error": "resource not found",
				})
				return

			case domain.ErrValidation:
				c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{
					"error": dErr.Msg,
				})
				return

			case domain.ErrStorageFailed:
				c.AbortWithStatusJSON(http.StatusServiceUnavailable, gin.H{
					"error": "storage service unavailable",
				})
				return
			}

			c.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{
				"error": "internal server error",
			})
			return
		}

		c.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{
			"error": "internal server error",
		})
	}
}
