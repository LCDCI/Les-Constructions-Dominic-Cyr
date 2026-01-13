package domain

import "context"

type FileService interface {
	Upload(ctx context.Context, input FileUploadInput) (FileMetadata, error)
	Get(ctx context.Context, fileID string) ([]byte, string, error)
	Delete(ctx context.Context, fileID string, deletedBy string) error
	ListByProjectID(ctx context.Context, projectID string) ([]FileMetadata, error)
	ReconcileStorageWithDatabase(ctx context.Context, projectID string) (int, error)
	AutoReconcileAllProjects(ctx context.Context) error
}
