package storage

import (
	"bytes"
	"context"
	"files-service/internal/config"
	"files-service/internal/domain"
	"log"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

type MinioStorage struct {
	client *minio.Client
	cfg    *config.Config
}

func NewMinioClient(cfg *config.Config) *MinioStorage {
	c, err := minio.New(cfg.MinioEndpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(cfg.MinioAccessKey, cfg.MinioSecretKey, ""),
		Secure: false, // Set to true if using HTTPS
	})
	if err != nil {
		log.Fatal(err)
	}

	// Optional: Ensure the bucket exists
	ctx := context.Background()
	found, err := c.BucketExists(ctx, cfg.MinioBucket)
	if err != nil {
		log.Fatalf("Error checking bucket %s: %v", cfg.MinioBucket, err)
	}
	if !found {
		err = c.MakeBucket(ctx, cfg.MinioBucket, minio.MakeBucketOptions{})
		if err != nil {
			log.Fatalf("Error creating bucket %s: %v", cfg.MinioBucket, err)
		}
		log.Printf("Successfully created bucket %s", cfg.MinioBucket)
	}

	return &MinioStorage{
		client: c,
		cfg:    cfg,
	}
}

// Upload accepts objectKey directly (instead of generating UUID here) to let the service control it.
func (m *MinioStorage) Upload(ctx context.Context, data []byte, key string, contentType string) (string, error) {

	_, err := m.client.PutObject(
		ctx,
		m.cfg.MinioBucket,
		key,
		bytes.NewReader(data),
		int64(len(data)),
		minio.PutObjectOptions{ContentType: contentType},
	)
	if err != nil {
		return "", domain.ErrStorageFailed
	}
	return key, nil
}

func (m *MinioStorage) Download(ctx context.Context, key string) ([]byte, error) {
	obj, err := m.client.GetObject(ctx, m.cfg.MinioBucket, key, minio.GetObjectOptions{})
	if err != nil {
		return nil, domain.ErrNotFound // Assuming storage error means file not found for download
	}
	defer obj.Close()

	buf := new(bytes.Buffer)
	_, err = buf.ReadFrom(obj)
	if err != nil {
		return nil, domain.ErrStorageFailed
	}

	return buf.Bytes(), nil
}

func (m *MinioStorage) Delete(ctx context.Context, key string) error {
	err := m.client.RemoveObject(ctx, m.cfg.MinioBucket, key, minio.RemoveObjectOptions{})
	if err != nil {
		return domain.ErrStorageFailed
	}
	return nil
}
