package domain

import "context"

type FileRepository interface {
	Save(ctx context.Context, f *File) error
	FindById(ctx context.Context, id string) (*File, error)
	Delete(ctx context.Context, id string, deletedBy string) error
	Archive(ctx context.Context, id string, archivedBy string) error
	Unarchive(ctx context.Context, id string) error
	FindByProjectID(ctx context.Context, projectID string) ([]File, error)
	FindByProjectIDAndRole(ctx context.Context, projectID, role, userId string) ([]File, error)
	FindArchivedByProjectID(ctx context.Context, projectID string) ([]File, error)
	FindByObjectKey(ctx context.Context, objectKey string) (*File, error)
}
