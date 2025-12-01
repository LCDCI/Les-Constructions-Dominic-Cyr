package service

import "context"

type StorageClient interface {
	Upload(ctx context.Context, data []byte, key string, contentType string) (string, error)
	Download(ctx context.Context, key string) ([]byte, error)
	Delete(ctx context.Context, key string) error
}
