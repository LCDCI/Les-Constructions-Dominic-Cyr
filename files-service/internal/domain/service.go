package domain

import "context"

type FileService interface {
	// Upload transforms input DTO to domain object, persists metadata, and uploads file content.
	Upload(ctx context.Context, input FileUploadInput) (FileMetadata, error)

	Get(ctx context.Context, fileID string) ([]byte, string, error)

	Delete(ctx context.Context, fileID string) error
}
