package service

import "context"

type StorageClient interface {
	Upload(ctx context.Context, data []byte, key string, contentType string, metadata map[string]string) (string, error)
	Download(ctx context.Context, key string) ([]byte, error)
	Delete(ctx context.Context, key string) error
	ListObjects(ctx context.Context, prefix string) ([]ObjectInfo, error)
}

// ObjectInfo mirrors minio.ObjectInfo for storage abstraction
type ObjectInfo struct {
	Key         string
	Size        int64
	ContentType string
	Metadata    map[string]string
}
