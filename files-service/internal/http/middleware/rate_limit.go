package middleware

import (
	"net/http"
	"sync"

	"files-service/internal/config"

	"github.com/gin-gonic/gin"
	"golang.org/x/time/rate"
)

// limiterStore caches per-key token buckets.
type limiterStore struct {
	m sync.Map
}

func (s *limiterStore) get(key string, rps float64, burst int) *rate.Limiter {
	if val, ok := s.m.Load(key); ok {
		return val.(*rate.Limiter)
	}
	lim := rate.NewLimiter(rate.Limit(rps), burst)
	s.m.Store(key, lim)
	return lim
}

// RateLimiter returns a middleware that enforces per-user (or IP) limits.
// Use stricter limits for write endpoints (uploads/deletes) via cfg.UploadRPS/UploadBurst.
func RateLimiter(cfg *config.Config) gin.HandlerFunc {
	reads := &limiterStore{}
	writes := &limiterStore{}

	return func(c *gin.Context) {
		if cfg.RateLimitRPS <= 0 || cfg.RateLimitBurst <= 0 {
			c.Next()
			return
		}

		// prefer authenticated user ID if available; otherwise fall back to client IP
		key := c.GetString("userId")
		if key == "" {
			key = c.ClientIP()
		}

		// choose limiter based on HTTP method (treat POST/PUT/PATCH/DELETE as writes)
		method := c.Request.Method
		var lim *rate.Limiter
		switch method {
		case http.MethodPost, http.MethodPut, http.MethodPatch, http.MethodDelete:
			rps := cfg.UploadRPS
			burst := cfg.UploadBurst
			if rps <= 0 || burst <= 0 {
				rps = cfg.RateLimitRPS
				burst = cfg.RateLimitBurst
			}
			lim = writes.get(key, rps, burst)
		default:
			lim = reads.get(key, cfg.RateLimitRPS, cfg.RateLimitBurst)
		}

		if !lim.Allow() {
			c.AbortWithStatusJSON(http.StatusTooManyRequests, gin.H{"error": "too many requests"})
			return
		}

		c.Next()
	}
}
