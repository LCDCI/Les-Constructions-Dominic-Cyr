package config

import (
	"database/sql"
	"log"
	"os"

	_ "github.com/lib/pq"
)

type Config struct {
	DBUrl          string
	MinioEndpoint  string
	MinioAccessKey string
	MinioSecretKey string
	MinioBucket    string
}

func Load() *Config {
	return &Config{
		DBUrl:          os.Getenv("DB_URL"),
		MinioEndpoint:  os.Getenv("MINIO_ENDPOINT"),
		MinioAccessKey: os.Getenv("MINIO_ACCESS_KEY"),
		MinioSecretKey: os.Getenv("MINIO_SECRET_KEY"),
		// Ensure this bucket exists in MinIO or create it at startup
		MinioBucket: "project-files",
	}
}

func InitPostgres(cfg *Config) *sql.DB {
	db, err := sql.Open("postgres", cfg.DBUrl)
	if err != nil {
		log.Fatal("Cannot connect to db:", err)
	}
	if err := db.Ping(); err != nil {
		log.Fatal("DB unreachable:", err)
	}
	return db
}
