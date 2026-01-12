package config

import (
	"database/sql"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"sort"
	"strconv"

	_ "github.com/lib/pq"
)

type Config struct {
	DBUrl          string
	MinioEndpoint  string
	MinioAccessKey string
	MinioSecretKey string
	MinioBucket    string
	MinioUseSSL    bool
	MinioRegion    string
	RateLimitRPS   float64
	RateLimitBurst int
	UploadRPS      float64
	UploadBurst    int
}

func Load() *Config {
	// Check for MINIO_USE_SSL environment variable (default: false for local MinIO, true for Spaces)
	useSSL := os.Getenv("MINIO_USE_SSL") == "true"

	// Get bucket name from env, fallback to "project-files" for backwards compatibility
	bucket := os.Getenv("MINIO_BUCKET")
	if bucket == "" {
		bucket = "project-files"
	}

	return &Config{
		DBUrl:          os.Getenv("DB_URL"),
		MinioEndpoint:  os.Getenv("MINIO_ENDPOINT"),
		MinioAccessKey: os.Getenv("MINIO_ACCESS_KEY"),
		MinioSecretKey: os.Getenv("MINIO_SECRET_KEY"),
		MinioBucket:    bucket,
		MinioUseSSL:    useSSL,
		MinioRegion:    os.Getenv("MINIO_REGION"),
		RateLimitRPS:   parseFloatEnv("RATE_LIMIT_RPS", 10),
		RateLimitBurst: parseIntEnv("RATE_LIMIT_BURST", 20),
		UploadRPS:      parseFloatEnv("RATE_LIMIT_UPLOAD_RPS", 3),
		UploadBurst:    parseIntEnv("RATE_LIMIT_UPLOAD_BURST", 6),
	}
}

// parseFloatEnv returns the parsed float64 value or the fallback on error/empty.
func parseFloatEnv(key string, fallback float64) float64 {
	v := os.Getenv(key)
	if v == "" {
		return fallback
	}
	if f, err := strconv.ParseFloat(v, 64); err == nil {
		return f
	}
	return fallback
}

// parseIntEnv returns the parsed int value or the fallback on error/empty.
func parseIntEnv(key string, fallback int) int {
	v := os.Getenv(key)
	if v == "" {
		return fallback
	}
	if i, err := strconv.Atoi(v); err == nil {
		return i
	}
	return fallback
}

func InitPostgres(cfg *Config) *sql.DB {
	db, err := sql.Open("postgres", cfg.DBUrl)
	if err != nil {
		log.Fatal("Cannot connect to db:", err)
	}
	if err := db.Ping(); err != nil {
		log.Fatal("DB unreachable:", err)
	}

	// Run migrations automatically at startup
	if err := runMigrations(db); err != nil {
		log.Printf("Warning: Migration execution failed: %v", err)
		// Don't fail startup, migrations might already be applied
	}

	return db
}

// runMigrations executes all SQL migration files in the migrations directory
func runMigrations(db *sql.DB) error {
	migrationsPath := "./migrations"

	// Check if migrations directory exists
	if _, err := os.Stat(migrationsPath); os.IsNotExist(err) {
		// Try alternative path for Docker
		migrationsPath = "/app/migrations"
		if _, err := os.Stat(migrationsPath); os.IsNotExist(err) {
			log.Println("No migrations directory found, skipping migrations")
			return nil
		}
	}

	// Read all .sql files from migrations directory
	files, err := filepath.Glob(filepath.Join(migrationsPath, "*.sql"))
	if err != nil {
		return fmt.Errorf("failed to read migration files: %w", err)
	}

	if len(files) == 0 {
		log.Println("No migration files found")
		return nil
	}

	// Sort files to ensure they run in order
	sort.Strings(files)

	log.Println("Running database migrations...")

	for _, file := range files {
		log.Printf("Applying migration: %s", filepath.Base(file))

		content, err := os.ReadFile(file)
		if err != nil {
			return fmt.Errorf("failed to read migration file %s: %w", file, err)
		}

		// Execute the migration
		_, err = db.Exec(string(content))
		if err != nil {
			// Log the error but don't fail - migration might already be applied
			log.Printf("Migration %s: %v (this is normal if already applied)", filepath.Base(file), err)
		} else {
			log.Printf("Migration %s: applied successfully", filepath.Base(file))
		}
	}

	log.Println("Database migrations completed")
	return nil
}
