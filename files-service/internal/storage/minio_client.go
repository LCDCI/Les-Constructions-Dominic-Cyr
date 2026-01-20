package storage

import (
	"bytes"
	"context"
	"files-service/internal/config"
	"files-service/internal/domain"
	"files-service/internal/service"
	"log"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

type MinioStorage struct {
	client *minio.Client
	cfg    *config.Config
}

func NewMinioClient(cfg *config.Config) *MinioStorage {
	log.Printf("Connecting to S3-compatible storage at %s (SSL: %v, Region: %s, Bucket: %s)",
		cfg.MinioEndpoint, cfg.MinioUseSSL, cfg.MinioRegion, cfg.MinioBucket)

	c, err := minio.New(cfg.MinioEndpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(cfg.MinioAccessKey, cfg.MinioSecretKey, ""),
		Secure: cfg.MinioUseSSL, // true for DigitalOcean Spaces (HTTPS), false for local MinIO
		Region: cfg.MinioRegion, // Required for DigitalOcean Spaces (e.g., "tor1")
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
func (m *MinioStorage) Upload(ctx context.Context, data []byte, key string, contentType string, metadata map[string]string) (string, error) {

	_, err := m.client.PutObject(
		ctx,
		m.cfg.MinioBucket,
		key,
		bytes.NewReader(data),
		int64(len(data)),
		minio.PutObjectOptions{
			ContentType:  contentType,
			UserMetadata: metadata,
		},
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

// ListObjects returns all objects with a given prefix from the bucket
func (m *MinioStorage) ListObjects(ctx context.Context, prefix string) ([]service.ObjectInfo, error) {
	opts := minio.ListObjectsOptions{
		Prefix:    prefix,
		Recursive: true,
	}

	var objects []service.ObjectInfo
	for objInfo := range m.client.ListObjects(ctx, m.cfg.MinioBucket, opts) {
		if objInfo.Err != nil {
			return nil, domain.ErrStorageFailed
		}

		// Fetch full object metadata
		statInfo, err := m.client.StatObject(ctx, m.cfg.MinioBucket, objInfo.Key, minio.StatObjectOptions{})
		if err != nil {
			log.Printf("[WARN] Failed to fetch metadata for %s: %v", objInfo.Key, err)
			objects = append(objects, service.ObjectInfo{
				Key:         objInfo.Key,
				Size:        objInfo.Size,
				ContentType: objInfo.ContentType,
				Metadata:    make(map[string]string),
			})
			continue
		}

		objects = append(objects, service.ObjectInfo{
			Key:         objInfo.Key,
			Size:        objInfo.Size,
			ContentType: statInfo.ContentType,
			Metadata:    statInfo.UserMetadata,
		})
	}

	return objects, nil
}
