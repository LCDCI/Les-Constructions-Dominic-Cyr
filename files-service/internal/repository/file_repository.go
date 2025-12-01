package repository

import (
	"context"
	"database/sql"
	"errors"
	"files-service/internal/domain"
)

type fileRepository struct {
	db *sql.DB
}

func NewFileRepository(db *sql.DB) domain.FileRepository {
	return &fileRepository{db: db}
}

func (r *fileRepository) Save(ctx context.Context, f *domain.File) error {
	_, err := r.db.ExecContext(ctx, `
		INSERT INTO files (id, file_name, content_type, category, size, object_key, created_at)
		VALUES ($1,$2,$3,$4,$5,$6,$7)
	`, f.ID, f.FileName, f.ContentType, f.Category, f.Size, f.ObjectKey, f.CreatedAt)
	return err
}

func (r *fileRepository) FindById(ctx context.Context, id string) (*domain.File, error) {
	f := domain.File{}
	err := r.db.QueryRowContext(ctx, `
		SELECT id,file_name,content_type,category,size,object_key,created_at
		FROM files WHERE id=$1
	`, id).Scan(
		&f.ID, &f.FileName, &f.ContentType,
		&f.Category, &f.Size, &f.ObjectKey, &f.CreatedAt,
	)
	if errors.Is(err, sql.ErrNoRows) {
		return nil, nil // Return nil, nil when not found
	}
	return &f, err
}

func (r *fileRepository) Delete(ctx context.Context, id string) error {
	_, err := r.db.ExecContext(ctx, `DELETE FROM files WHERE id=$1`, id)
	return err
}
