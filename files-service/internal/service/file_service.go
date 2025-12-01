package service

import (
	"context"
	"files-service/internal/domain"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
)

type fileService struct {
	repo    domain.FileRepository
	storage StorageClient
}

func NewFileService(repo domain.FileRepository, storage StorageClient) domain.FileService {
	return &fileService{
		repo:    repo,
		storage: storage,
	}
}

func (s *fileService) Upload(ctx context.Context, input domain.FileUploadInput) (domain.FileMetadata, error) {

	// --- BASIC VALIDATION ---
	if input.FileName == "" || input.ContentType == "" || len(input.Data) == 0 {
		return domain.FileMetadata{}, domain.ErrValidation
	}

	if !input.Category.Validate() {
		return domain.FileMetadata{}, domain.ErrValidation
	}

	projectFolder := strings.TrimSpace(input.ProjectID)
	if projectFolder == "" {
		projectFolder = "unassigned"
	}

	id := uuid.New().String()

	objectKey := fmt.Sprintf(
		"%s/%s/%s",
		projectFolder,
		strings.ToLower(string(input.Category)),
		id,
	)

	// Upload to MinIO
	_, err := s.storage.Upload(ctx, input.Data, objectKey, input.ContentType)
	if err != nil {
		return domain.FileMetadata{}, domain.ErrStorageFailed
	}

	// Build domain entity
	file := domain.File{
		ID:          id,
		ProjectID:   input.ProjectID,
		FileName:    input.FileName,
		UploadedBy:  input.UploadedBy,
		ContentType: input.ContentType,
		Category:    input.Category,
		Size:        int64(len(input.Data)),
		ObjectKey:   objectKey,
		CreatedAt:   time.Now(),
	}

	// Save to DB
	if err := s.repo.Save(ctx, &file); err != nil {

		_ = s.storage.Delete(ctx, objectKey)
		return domain.FileMetadata{}, err
	}

	return domain.FileMetadata{
		ID:          id,
		FileName:    file.FileName,
		ContentType: file.ContentType,
		Category:    file.Category,
		ProjectID:   file.ProjectID,
		UploadedBy:  file.UploadedBy,
		Url:         "/files/" + id,
	}, nil
}

func (s *fileService) Get(ctx context.Context, fileID string) ([]byte, string, error) {

	// Load metadata first
	f, err := s.repo.FindById(ctx, fileID)
	if err != nil {
		return nil, "", err
	}
	if f == nil {
		return nil, "", domain.ErrNotFound
	}

	// Download blob
	data, err := s.storage.Download(ctx, f.ObjectKey)
	if err != nil {
		return nil, "", err
	}

	return data, f.ContentType, nil
}

func (s *fileService) Delete(ctx context.Context, fileID string) error {

	f, err := s.repo.FindById(ctx, fileID)
	if err != nil {
		return err
	}
	if f == nil {
		return domain.ErrNotFound
	}

	// Delete blob from storage
	if err := s.storage.Delete(ctx, f.ObjectKey); err != nil {
		return err
	}

	// Delete row (hard delete for now)
	return s.repo.Delete(ctx, fileID)
}
