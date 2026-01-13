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
		INSERT INTO files (id, file_name, content_type, category, size, object_key, created_at, project_id, uploaded_by, is_active)
		VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10)
	`, f.ID, f.FileName, f.ContentType, f.Category, f.Size, f.ObjectKey, f.CreatedAt, f.ProjectID, f.UploadedBy, true)
	return err
}

func (r *fileRepository) FindById(ctx context.Context, id string) (*domain.File, error) {
	f := domain.File{}
	err := r.db.QueryRowContext(ctx, `
		SELECT id, file_name, content_type, category, size, object_key, created_at, project_id, uploaded_by, is_active
		FROM files WHERE id=$1 AND is_active=true
	`, id).Scan(
		&f.ID, &f.FileName, &f.ContentType,
		&f.Category, &f.Size, &f.ObjectKey, &f.CreatedAt,
		&f.ProjectID, &f.UploadedBy, &f.IsActive,
	)
	if errors.Is(err, sql.ErrNoRows) {
		return nil, nil
	}
	return &f, err
}

// Delete performs a soft delete on the file record.
// Instead of physically removing the record from the database, this method marks the file as inactive (is_active = false)
// and records audit information (deleted_at timestamp and deleted_by user). This preserves the record for audit and
// traceability purposes, ensuring that deleted files can be tracked and reviewed if necessary.
func (r *fileRepository) Delete(ctx context.Context, id string, deletedBy string) error {
	result, err := r.db.ExecContext(ctx, `UPDATE files SET is_active = false, deleted_at = NOW(), deleted_by = $2 WHERE id = $1 AND is_active = true`, id, deletedBy)
	if err != nil {
		return err
	}
	rowsAffected, err := result.RowsAffected()
	if err != nil {
		return err
	}
	if rowsAffected == 0 {
		return sql.ErrNoRows
	}
	return nil
}

func (r *fileRepository) FindByProjectID(ctx context.Context, projectID string) ([]domain.File, error) {
	rows, err := r.db.QueryContext(ctx, `
		SELECT id, file_name, content_type, category, size, object_key, created_at, project_id, uploaded_by, is_active
		FROM files WHERE project_id=$1 AND is_active=true ORDER BY created_at DESC
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
			&f.ProjectID, &f.UploadedBy, &f.IsActive,
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

// FindByObjectKey retrieves a file by its storage object key
func (r *fileRepository) FindByObjectKey(ctx context.Context, objectKey string) (*domain.File, error) {
	f := domain.File{}
	err := r.db.QueryRowContext(ctx, `
		SELECT id, file_name, content_type, category, size, object_key, created_at, project_id, uploaded_by, is_active
		FROM files WHERE object_key=$1 AND is_active=true
	`, objectKey).Scan(
		&f.ID, &f.FileName, &f.ContentType,
		&f.Category, &f.Size, &f.ObjectKey, &f.CreatedAt,
		&f.ProjectID, &f.UploadedBy, &f.IsActive,
	)
	if errors.Is(err, sql.ErrNoRows) {
		return nil, nil
	}
	return &f, err
}
