package domain

import "context"

type FileRepository interface {
	Save(ctx context.Context, f *File) error
	FindById(ctx context.Context, id string) (*File, error)
	Delete(ctx context.Context, id string) error
}
