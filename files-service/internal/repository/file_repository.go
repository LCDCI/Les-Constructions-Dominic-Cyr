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
		INSERT INTO files (id, file_name, content_type, category, size, object_key, created_at, project_id, uploaded_by)
		VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9)
	`, f.ID, f.FileName, f.ContentType, f.Category, f.Size, f.ObjectKey, f.CreatedAt, f.ProjectID, f.UploadedBy)
	return err
}

func (r *fileRepository) FindById(ctx context.Context, id string) (*domain.File, error) {
	f := domain.File{}
	err := r.db.QueryRowContext(ctx, `
		SELECT id, file_name, content_type, category, size, object_key, created_at, project_id, uploaded_by
		FROM files WHERE id=$1
	`, id).Scan(
		&f.ID, &f.FileName, &f.ContentType,
		&f.Category, &f.Size, &f.ObjectKey, &f.CreatedAt,
		&f.ProjectID, &f.UploadedBy,
	)
	if errors.Is(err, sql.ErrNoRows) {
		return nil, nil
	}
	return &f, err
}

func (r *fileRepository) Delete(ctx context.Context, id string) error {
	_, err := r.db.ExecContext(ctx, `DELETE FROM files WHERE id=$1`, id)
	return err
}

func (r *fileRepository) FindByProjectID(ctx context.Context, projectID string) ([]domain.File, error) {
	rows, err := r.db.QueryContext(ctx, `
		SELECT id, file_name, content_type, category, size, object_key, created_at, project_id, uploaded_by
		FROM files WHERE project_id=$1 ORDER BY created_at DESC
	`, projectID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var files []domain.File
	for rows.Next() {
		f := domain.File{}
		err := rows.Scan(
			&f.ID, &f.FileName, &f.ContentType,
			&f.Category, &f.Size, &f.ObjectKey, &f.CreatedAt,
			&f.ProjectID, &f.UploadedBy,
		)
		if err != nil {
			return nil, err
		}
		files = append(files, f)
	}

	if err := rows.Err(); err != nil {
		return nil, err
	}

	return files, nil
}
